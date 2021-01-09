package dev.hbeck.kdl.parse;

import dev.hbeck.kdl.objects.KDLBoolean;
import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLNull;
import dev.hbeck.kdl.objects.KDLNumber;
import dev.hbeck.kdl.objects.KDLObject;
import dev.hbeck.kdl.objects.KDLProperty;
import dev.hbeck.kdl.objects.KDLString;
import dev.hbeck.kdl.objects.KDLValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KDLParserV2 {

    public static final int EOF = -1;
    public static final int MAX_UNICODE = 0x10FFF;

    private static final Set<Integer> NUMERIC_START_CHARS =
            Stream.of('+', '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
                    .map(character -> (int) character)
                    .collect(Collectors.toSet());

    private static final Set<Integer> INVALID_BARE_ID_CHARS = Stream.of('\n', '\u000C', '\r', '\u0085', '\u2028', '\u2029',
            '\\', '{', '}', '<', '>', ';', '[', ']', '=', ',', '"', '#', '\u0009', '\u0020', '\u00A0', '\u1680', '\u2000',
            '\u2001', '\u2002', '\u2003', '\u2004', '\u2005', '\u2006', '\u2007', '\u2008', '\u2009', '\u200A', '\u202F',
            '\u205F', '\u3000')
            .map(character -> (int) character)
            .collect(Collectors.toSet());

    private static final Set<Integer> INVALID_BARE_ID_START_CHARS = Stream.concat(INVALID_BARE_ID_CHARS.stream(),
            Stream.of((int) '0', (int) '1', (int) '2', (int) '3', (int) '4', (int) '5', (int) '6', (int) '7', (int) '8', (int) '9'))
            .collect(Collectors.toSet());

    private static final Set<Integer> DECIMAL_CHARS =
            Stream.of('+', '-', 'e', 'E', '.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
                    .map(character -> (int) character)
                    .collect(Collectors.toSet());

    private static final Set<Integer> HEX_CHARS =
            Stream.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd', 'E', 'e', 'F', 'f')
                    .map(character -> (int) character)
                    .collect(Collectors.toSet());

    private static final Set<Integer> OCTAL_CHARS =
            Stream.of('0', '1', '2', '3', '4', '5', '6', '7')
                    .map(character -> (int) character)
                    .collect(Collectors.toSet());

    private static final Set<Integer> BINARY_CHARS = Stream.of('0', '1')
            .map(character -> (int) character)
            .collect(Collectors.toSet());

    private static final Set<Integer> LITERAL_CHARS = Stream.of('t', 'r', 'u', 'e', 'n', 'l', 'f', 'a', 's')
            .map(character -> (int) character)
            .collect(Collectors.toSet());

    private static final Set<Integer> UNICODE_LINESPACE = Stream.of('\r', '\n', '\u0085', '\u000C', '\u2028', '\u2029')
            .map(character -> (int) character)
            .collect(Collectors.toSet());

    private static final Set<Integer> UNICODE_WHITESPACE =
            Stream.of('\u0009', '\u0020', '\u00A0', '\u1680', '\u2000', '\u2001', '\u2002', '\u2003', '\u2004', '\u2005',
                    '\u2006', '\u2007', '\u2008', '\u2009', '\u200A', '\u202F', '\u205F', '\u3000')
                    .map(character -> (int) character)
                    .collect(Collectors.toSet());

    private enum WhitespaceResult {
        NO_WHITESPACE,
        END_NODE,
        SKIP_NEXT,
        NODE_SPACE
    }

    private enum SlashAction {
        END_NODE,
        SKIP_NEXT,
        NOTHING
    }

    public KDLDocument parse(Reader reader) throws IOException {
        final KDLParseContext context = new KDLParseContext(reader);

        try {
            return parseDocument(context, true);
        } catch (KDLParseException e) {
            final String message = String.format("%s\n%s", e.getMessage(), context.getCurrentPosition());
            throw new KDLParseException(message, e);
        } catch (IOException e) {
            throw new IOException(e);
        } catch (Throwable t) {
            throw new KDLParseException(String.format("Unexpected exception:\n%s", context.getCurrentPosition()), t);
        }
    }

    public KDLDocument parse(InputStream stream) throws IOException {
        return parse(new InputStreamReader(stream));
    }

    public KDLDocument parse(String string) {
        final StringReader reader = new StringReader(string);
        try {
            return parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private KDLDocument parseDocument(KDLParseContext context, boolean root) throws IOException {
        int c = context.peek();
        if (c == EOF) {
            return new KDLDocument(Collections.emptyList());
        }

        final ArrayList<KDLNode> nodes = new ArrayList<>();
        while (true) {
            boolean skippingNode = false;
            switch (consumeWhitespaceAndLinespace(context)) {
                case NODE_SPACE:
                case NO_WHITESPACE:
                    break;
                case END_NODE:
                    c = context.peek();
                    if (c == EOF) {
                        break;
                    } else {
                        continue;
                    }
                case SKIP_NEXT:
                    skippingNode = true;
                    break;
            }

            c = context.peek();
            if (c == EOF) {
                if (root) {
                    return new KDLDocument(nodes);
                } else {
                    throw new KDLParseException("Got EOF, expected a node or '}'");
                }
            } else if (c == '}') {
                if (root) {
                    throw new KDLParseException("Unexpected '}' in root document");
                } else {
                    return new KDLDocument(nodes);
                }
            }

            final Optional<KDLNode> node = parseNode(context);
            if (!skippingNode && node.isPresent()) {
                nodes.add(node.get());
            }
        }
    }

    private Optional<KDLNode> parseNode(KDLParseContext context) throws IOException {
        final List<KDLValue> args = new ArrayList<>();
        final Map<String, KDLValue> properties = new TreeMap<>();
        Optional<KDLDocument> child = Optional.empty();

        int c = context.peek();
        if (c == '}') {
            return Optional.empty();
        }

        final String identifier = parseIdentifier(context);
        while (true) {
            final WhitespaceResult whitespaceResult = consumeWhitespaceAndBlockComments(context);
            c = context.peek();
            switch (whitespaceResult) {
                case NODE_SPACE:
                    if (c == '{') {
                        child = Optional.of(parseChild(context));
                        return Optional.of(new KDLNode(identifier, properties, args, child));
                    } else if (UNICODE_LINESPACE.contains(c)) {
                        return Optional.of(new KDLNode(identifier, properties, args, child));
                    } else {
                        final KDLObject object = parseArgOrProp(context);
                        if (object instanceof KDLValue) {
                            args.add((KDLValue) object);
                        } else if (object instanceof KDLProperty) {
                            final KDLProperty property = (KDLProperty) object;
                            properties.put(property.getKey(), property.getValue());
                        } else {
                            throw new KDLInternalException(
                                    String.format("Unexpected type found, expected property, arg, or child: '%s' type: %s",
                                            object.toKDL(), object.getClass().getSimpleName()));
                        }
                    }
                    break;

                case NO_WHITESPACE:
                    if (c == '{') {
                        child = Optional.of(parseChild(context));
                        return Optional.of(new KDLNode(identifier, properties, args, child));
                    } else if (UNICODE_LINESPACE.contains(c) || c == EOF) {
                        return Optional.of(new KDLNode(identifier, properties, args, child));
                    } else {
                        throw new KDLParseException(String.format("Unexpected character: '%s'", (char) c));
                    }
                case END_NODE:
                    return Optional.of(new KDLNode(identifier, properties, args, child));
                case SKIP_NEXT:
                    if (c == '{') {
                        parseChild(context);
                        return Optional.of(new KDLNode(identifier, properties, args, child));
                    } else if (UNICODE_LINESPACE.contains(c)) {
                        throw new KDLParseException("Unexpected skip marker before newline");
                    } else {
                        final KDLObject object = parseArgOrProp(context);
                        if (!(object instanceof KDLValue) && !(object instanceof KDLProperty)) {
                            throw new KDLInternalException(
                                    String.format("Unexpected type found, expected property, arg, or child: '%s' type: %s",
                                            object.toKDL(), object.getClass().getSimpleName()));
                        }
                    }
                    break;
            }
        }
    }

    private String parseIdentifier(KDLParseContext context) throws IOException {
        int c = context.peek();
        if (c == '"') {
            return parseEscapedString(context);
        } else if (!INVALID_BARE_ID_START_CHARS.contains(c)) {
            if (c == 'r') {
                context.read();
                int next = context.peek();
                context.unread('r');
                if (next == '"' || next == '#') {
                    return parseRawString(context);
                } else {
                    return parseBareIdentifier(context);
                }
            } else {
                return parseBareIdentifier(context);
            }
        } else {
            throw new KDLParseException("");
        }
    }

    private KDLObject parseArgOrProp(KDLParseContext context) throws IOException {
        final KDLObject object;
        int c = context.peek();
        if (c == '"') {
            object = new KDLString(parseEscapedString(context));
        } else if (NUMERIC_START_CHARS.contains(c)) {
            object = parseNumber(context);
        } else if (!INVALID_BARE_ID_START_CHARS.contains(c)) {
            boolean isBare = false;
            String strVal;
            if (c == 'r') {
                context.read();
                int next = context.peek();
                if (next == '"' || next == '#') {
                    context.unread('r');
                    strVal = parseRawString(context);
                } else {
                    isBare = true;
                    strVal = parseBareIdentifier(context);
                }
            } else {
                isBare = true;
                strVal = parseBareIdentifier(context);
            }

            if (isBare) {
                if ("true".equals(strVal)) {
                    object = KDLBoolean.TRUE;
                } else if ("false".equals(strVal)) {
                    object = KDLBoolean.FALSE;
                } else if ("null".equals(strVal)) {
                    object = KDLNull.INSTANCE;
                } else {
                    object = new KDLString(strVal);
                }
            } else {
                object = new KDLString(strVal);
            }
        } else {
            throw new KDLParseException(String.format("Unexpected character: '%s'", (char) c));
        }

        if (object instanceof KDLString) {
            c = context.peek();
            if (c == '=') {
                context.read();
                final KDLValue value = parseValue(context);
                return new KDLProperty(((KDLString) object).getValue(), value);
            } else {
                return object;
            }
        } else {
            return object;
        }
    }

    private KDLDocument parseChild(KDLParseContext context) throws IOException {
        int c = context.read();
        if (c != '{') {
            throw new KDLInternalException("");
        }

        final KDLDocument document = parseDocument(context, false);

        switch (consumeWhitespaceAndLinespace(context)) {
            case END_NODE:
                throw new KDLInternalException("");
            case SKIP_NEXT:
                throw new KDLParseException("");
            default:
                //Fall through
        }

        c = context.read();
        if (c != '}') {
            throw new KDLParseException("No closing brace found for child");
        }

        return document;
    }

    private KDLValue parseValue(KDLParseContext context) throws IOException {
        int c = context.peek();
        if (c == '"') {
            return new KDLString(parseEscapedString(context));
        } else if (c == 'r') {
            return new KDLString(parseRawString(context));
        } else if (NUMERIC_START_CHARS.contains(c)) {
            return parseNumber(context);
        } else {
            final StringBuilder stringBuilder = new StringBuilder();

            while (LITERAL_CHARS.contains(c)) {
                context.read();
                stringBuilder.appendCodePoint(c);
                c = context.peek();
            }

            final String strVal = stringBuilder.toString();
            switch (strVal) {
                case "true":
                    return KDLBoolean.TRUE;
                case "false":
                    return KDLBoolean.FALSE;
                case "null":
                    return KDLNull.INSTANCE;
                default:
                    throw new KDLParseException(String.format("Unknown literal in property value: '%s'", strVal));
            }
        }
    }

    private KDLNumber parseNumber(KDLParseContext context) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        final int radix;
        final Set<Integer> legalChars;

        int c = context.peek();
        if (c == '0') {
            context.read();
            c = context.peek();
            if (c == 'x') {
                context.read();
                radix = 16;
                legalChars = HEX_CHARS;
            } else if (c == 'o') {
                context.read();
                radix = 8;
                legalChars = OCTAL_CHARS;
            } else if (c == 'b') {
                context.read();
                radix = 2;
                legalChars = BINARY_CHARS;
            } else {
                radix = 10;
                legalChars = DECIMAL_CHARS;
            }
        } else {
            radix = 10;
            legalChars = DECIMAL_CHARS;
        }

        c = context.peek();
        if (c == '_') {
            throw new KDLParseException("");
        }

        c = context.peek();
        while (legalChars.contains(c) || c == '_') {
            context.read();
            if (c != '_') {
                stringBuilder.appendCodePoint(c);
            }
            c = context.peek();
        }

        final String str = stringBuilder.toString();
        final BigDecimal value;
        try {
            if (radix == 10) {
                value = new BigDecimal(str);
            } else {
                value = new BigDecimal(new BigInteger(str, radix));
            }
        } catch (NumberFormatException e) {
            throw new KDLParseException(String.format("Couldn't parse '%s' as a number with radix %d", stringBuilder.toString(), radix));
        }

        return new KDLNumber(value, radix);
    }

    private String parseBareIdentifier(KDLParseContext context) throws IOException {
        int c = context.read();
        if (INVALID_BARE_ID_START_CHARS.contains(c)) {
            throw new KDLParseException("");
        }

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.appendCodePoint(c);

        c = context.peek();
        while (!INVALID_BARE_ID_CHARS.contains(c) && c != EOF) {
            stringBuilder.appendCodePoint(context.read());
            c = context.peek();
        }

        return stringBuilder.toString();
    }

    private String parseEscapedString(KDLParseContext context) throws IOException {
        int c = context.read();
        if (c != '"') {
            throw new KDLParseException("No quote at the beginning of escaped string");
        }

        final StringBuilder stringBuilder = new StringBuilder();
        boolean inEscape = false;
        while (true) {
            c = context.read();
            if (c == '\\') {
                inEscape = true;
            } else if (c == '"' && !inEscape) {
                return stringBuilder.toString();
            } else if (inEscape) {
                stringBuilder.append((char) getEscaped(c, context));
                inEscape = false;
            } else if (c == EOF) {
                throw new KDLParseException("EOF while reading an escaped string");
            } else {
                stringBuilder.appendCodePoint(c);
            }
        }
    }

    private int getEscaped(int c, KDLParseContext context) throws IOException {
        switch (c) {
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case '\\':
                return '\\';
            case '"':
                return '\"';
            case 'b':
                return '\b';
            case 'f':
                return '\f';
            case 'u': {
                final StringBuilder stringBuilder = new StringBuilder(6);
                c = context.read();
                if (c != '{') {
                    throw new KDLParseException("Unicode escape sequences must be surround by {} brackets");
                }

                stringBuilder.appendCodePoint(c);
                c = context.read();
                while (c != '}') {
                    if (c == EOF) {
                        throw new KDLParseException("Reached EOF while reading unicode escape sequence");
                    } else if (!HEX_CHARS.contains(c)) {
                        throw new KDLParseException(String.format("Unicode escape sequences must be valid hex chars, got: '%s'", (char) c));
                    }

                    stringBuilder.appendCodePoint(c);
                    c = context.read();
                }

                final String strCode = stringBuilder.toString();
                if (strCode.isEmpty() || strCode.length() > 6) {
                    throw new KDLParseException(String.format("Unicode escape sequences must be between 1 and 6 characters in length. Got: '%s'", strCode));
                }

                final int code;
                try {
                    code = Integer.parseInt(strCode, 16);
                } catch (NumberFormatException e) {
                    throw new KDLParseException(String.format("Couldn't parse '%s' as a hex integer", strCode));
                }

                if (code < 0 || MAX_UNICODE < code) {
                    throw new KDLParseException(String.format("Unicode code point is outside allowed range [0, %x]: %x", MAX_UNICODE, code));
                } else {
                    return code;
                }
            }
            default:
                throw new KDLParseException(String.format("Illegal escape sequence: '\\%s'", (char) c));
        }
    }

    private String parseRawString(KDLParseContext context) throws IOException {
        int c = context.read();
        if (c != 'r') {
            throw new KDLInternalException("Raw string should start with 'r'");
        }

        int hashDepth = 0;
        c = context.read();
        while (c == '#') {
            hashDepth++;
            c = context.read();
        }

        if (c != '"') {
            throw new KDLParseException("Malformed raw string");
        }
//node_2 r#""arg\n""#
        final StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            c = context.read();
            if (c == '"') {
                StringBuilder subStringBuilder = new StringBuilder();
                subStringBuilder.append('"');
                int hashDepthHere = 0;
                while (true) {
                    c = context.peek();
                    if (c == '#') {
                        context.read();
                        hashDepthHere++;
                        subStringBuilder.append('#');
                    } else {
                        break;
                    }
                }

                if (hashDepthHere < hashDepth) {
                    stringBuilder.append(subStringBuilder);
                } else if (hashDepthHere == hashDepth) {
                    return stringBuilder.toString();
                } else if (hashDepthHere > hashDepth) {
                    for (int i = 0; i < hashDepthHere - hashDepth; i++) {
                        stringBuilder.append('#');
                    }
                }

            } else {
                stringBuilder.appendCodePoint(c);
            }
        }
    }

    private SlashAction getSlashAction(KDLParseContext context) throws IOException {
        int c = context.read();
        if (c != '/') {
            throw new KDLParseException("");
        }

        c = context.read();
        switch (c) {
            case '-':
                return SlashAction.SKIP_NEXT;
            case '*':
                consumeBlockComment(context);
                return SlashAction.NOTHING;
            case '/':
                consumeLineComment(context);
                return SlashAction.END_NODE;
            default:
                throw new KDLParseException(String.format("Unexpected character: '%s'", (char) c));
        }
    }

    private WhitespaceResult consumeWhitespaceAndBlockComments(KDLParseContext context) throws IOException {
        boolean skipping = false;
        boolean foundWhitespace = false;
        int c = context.peek();
        while (UNICODE_WHITESPACE.contains(c) || c == '/' || c == '\\') {
            if (c == '/') {
                switch (getSlashAction(context)) {
                    case END_NODE:
                        return WhitespaceResult.END_NODE;

                    case SKIP_NEXT:
                        if (skipping) {
                            throw new KDLParseException("Node/Token skip may only be specified once per node/token");
                        } else {
                            skipping = true;
                        }
                        break;

                    case NOTHING:
                        foundWhitespace = true;
                        break;
                }
            } else {
                foundWhitespace |= consumeWhitespace(context);
            }

            c = context.peek();
        }

        if (skipping) {
            return WhitespaceResult.SKIP_NEXT;
        } else if (foundWhitespace) {
            return WhitespaceResult.NODE_SPACE;
        } else {
            return WhitespaceResult.NO_WHITESPACE;
        }
    }

    private void consumeLineComment(KDLParseContext context) throws IOException {
        int c = context.read();
        boolean lastCharIsCarriageReturn;
        while (!UNICODE_LINESPACE.contains(c) && c != EOF) {
            lastCharIsCarriageReturn = c == '\r';
            c = context.read();

            if (c == '\n' && lastCharIsCarriageReturn) {
                return;
            }
        }

        if (c != EOF) {
            context.unread(c);
        }
    }

    private void consumeBlockComment(KDLParseContext context) throws IOException {
        while (true) {
            int c = context.read();
            while (c != '/' && c != '*' && c != EOF) {
                c = context.read();
            }

            if (c == EOF) {
                throw new KDLParseException("Got EOF while reading block comment");
            }

            if (c == '/') {
                c = context.peek();
                if (c == '*') {
                    context.read();
                    consumeBlockComment(context);
                }
            } else { // c == '*'
                c = context.read();
                if (c == '/') {
                    return;
                } else {
                    context.unread(c);
                }
            }
        }
    }

    private boolean consumeWhitespace(KDLParseContext context) throws IOException {
        boolean foundWhitespace = false;
        int c = context.peek();
        while (UNICODE_WHITESPACE.contains(c) || c == '\\') {
            foundWhitespace = true;
            if (c == '\\') {
                context.read();
                c = context.read();
                if (c == '\n') {
                } else if (c == '\r') {
                    c = context.peek();
                    if (c == '\n') {
                        context.read();
                    }
                } else if (!UNICODE_LINESPACE.contains(c)) {
                    throw new KDLParseException("");
                }
            } else {
                context.read();
                c = context.peek();
            }
        }

        return foundWhitespace;
    }

    private WhitespaceResult consumeWhitespaceAndLinespace(KDLParseContext context) throws IOException {
        boolean skipNext = false;
        while (true) {
            int c = context.peek();
            boolean isLinespace = UNICODE_LINESPACE.contains(c);
            while (UNICODE_WHITESPACE.contains(c) || isLinespace) {
                context.read();
                if (isLinespace && skipNext) {
                    throw new KDLParseException("Unexpected newline after skip marker");
                }
                c = context.peek();
                isLinespace = UNICODE_LINESPACE.contains(c);
            }

            if (c == '/') {
                switch (getSlashAction(context)) {
                    case END_NODE:
                    case NOTHING:
                        //Nothing to do
                        break;
                    case SKIP_NEXT:
                        skipNext = true;
                }
            } else if (c == EOF) {
                if (skipNext) {
                    throw new KDLParseException("Unexpected EOF after skip marker");
                } else {
                    return WhitespaceResult.END_NODE;
                }
            } else {
                if (skipNext) {
                    return WhitespaceResult.SKIP_NEXT;
                } else {
                    return WhitespaceResult.NODE_SPACE;
                }
            }
        }
    }
}
