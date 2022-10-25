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
import java.util.TreeMap;
import java.util.function.Predicate;

import static dev.hbeck.kdl.parse.CharClasses.isLiteralChar;
import static dev.hbeck.kdl.parse.CharClasses.isUnicodeLinespace;
import static dev.hbeck.kdl.parse.CharClasses.isUnicodeWhitespace;
import static dev.hbeck.kdl.parse.CharClasses.isValidBareIdChar;
import static dev.hbeck.kdl.parse.CharClasses.isValidBareIdStart;
import static dev.hbeck.kdl.parse.CharClasses.isValidDecimalChar;
import static dev.hbeck.kdl.parse.CharClasses.isValidHexChar;
import static dev.hbeck.kdl.parse.CharClasses.isValidNumericStart;

/**
 * The core parser object. Instances are stateless and safe to share between threads.
 */
public class KDLParser {

    public static final int EOF = -1;
    public static final int MAX_UNICODE = 0x10FFFF;

    enum WhitespaceResult {
        NO_WHITESPACE,
        END_NODE,
        SKIP_NEXT,
        NODE_SPACE
    }

    enum SlashAction {
        END_NODE,
        SKIP_NEXT,
        NOTHING
    }

    /**
     * Parse the given stream into a KDLDocument model object.
     *
     * @param reader the stream reader to parse from
     * @return the parsed document
     * @throws IOException if any error occurs while reading the stream
     * @throws KDLParseException if the document is invalid for any reason
     */
    public KDLDocument parse(Reader reader) throws IOException {
        final KDLParseContext context = new KDLParseContext(reader);

        try {
            return parseDocument(context, true);
        } catch (KDLParseException e) {
            final String message = String.format("%s\n%s", e.getMessage(), context.getErrorLocationAndInvalidateContext());
            throw new KDLParseException(message, e);
        } catch (IOException e) {
            throw new IOException(context.getErrorLocationAndInvalidateContext(), e);
        } catch (KDLInternalException e) {
            throw new KDLInternalException(context.getErrorLocationAndInvalidateContext(), e);
        } catch (Throwable t) {
            throw new KDLInternalException(String.format("Unexpected exception:\n%s", context.getErrorLocationAndInvalidateContext()), t);
        }
    }

    /**
     * Parse the given stream into a KDLDocument model object.
     *
     * @param stream the stream to parse from
     * @return the parsed document
     * @throws IOException if any error occurs while reading the stream
     * @throws KDLParseException if the document is invalid for any reason
     */
    public KDLDocument parse(InputStream stream) throws IOException {
        return parse(new InputStreamReader(stream));
    }

    /**
     * Parse the given string into a KDLDocument model object.
     *
     * @param string the string to parse
     * @return the parsed document
     * @throws KDLParseException if the document is invalid for any reason
     */
    public KDLDocument parse(String string) {
        final StringReader reader = new StringReader(string);
        try {
            return parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    KDLDocument parseDocument(KDLParseContext context, boolean root) throws IOException {
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
            consumeAfterNode(context);
            if (!skippingNode && node.isPresent()) {
                nodes.add(node.get());
            }
        }
    }

    Optional<KDLNode> parseNode(KDLParseContext context) throws IOException {
        final List<KDLValue<?>> args = new ArrayList<>();
        final Map<String, KDLValue<?>> properties = new TreeMap<>();
        Optional<KDLDocument> child = Optional.empty();

        int c = context.peek();
        if (c == '}') {
            return Optional.empty();
        }

        final Optional<String> type = parseTypeIfPresent(context);
        final String identifier = parseIdentifier(context);
        while (true) {
            final WhitespaceResult whitespaceResult = consumeWhitespaceAndBlockComments(context);
            c = context.peek();
            switch (whitespaceResult) {
                case NODE_SPACE:
                    if (c == '{') {
                        child = Optional.of(parseChild(context));
                        return Optional.of(new KDLNode(identifier, type, properties, args, child));
                    } else if (isUnicodeLinespace(c)) {
                        return Optional.of(new KDLNode(identifier, type, properties, args, child));
                    } if (c == EOF) {
                        return Optional.of(new KDLNode(identifier, type, properties, args, child));
                    } else {
                        final KDLObject object = parseArgOrProp(context);
                        if (object instanceof KDLValue) {
                            args.add((KDLValue<?>) object);
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
                        return Optional.of(new KDLNode(identifier, type, properties, args, child));
                    } else if (isUnicodeLinespace(c) || c == EOF) {
                        return Optional.of(new KDLNode(identifier, type, properties, args, child));
                    } else if (c == ';') {
                        context.read();
                        return Optional.of(new KDLNode(identifier, type, properties, args, child));
                    } else {
                        throw new KDLParseException(String.format("Unexpected character: '%s' (\\u%06X)", (char) c, c));
                    }
                case END_NODE:
                    return Optional.of(new KDLNode(identifier, type, properties, args, child));
                case SKIP_NEXT:
                    if (c == '{') {
                        parseChild(context); //Ignored
                        return Optional.of(new KDLNode(identifier, type, properties, args, child));
                    } else if (isUnicodeLinespace(c)) {
                        throw new KDLParseException("Unexpected skip marker before newline");
                    } else if ( c == EOF) {
                        throw new KDLParseException("Unexpected EOF following skip marker");
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

    String parseIdentifier(KDLParseContext context) throws IOException {
        int c = context.peek();
        if (c == '"') {
            return parseEscapedString(context);
        } else if (isValidBareIdStart(c)) {
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
            throw new KDLParseException(String.format("Expected an identifier, but identifiers can't start with '%s'", (char) c));
        }
    }

    KDLObject parseArgOrProp(KDLParseContext context) throws IOException {
        final KDLObject object;
        final Optional<String> type = parseTypeIfPresent(context);
        boolean isBare = false;
        int c = context.peek();
        if (c == '"') {
            object = new KDLString(parseEscapedString(context), type);
        } else if (isValidNumericStart(c)) {
            object = parseNumber(context, type);
        } else if (isValidBareIdStart(c)) {
            String strVal;
            if (c == 'r') {
                context.read();
                int next = context.peek();
                context.unread('r');
                if (next == '"' || next == '#') {
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
                    object = new KDLBoolean(true, type);
                } else if ("false".equals(strVal)) {
                    object = new KDLBoolean(false, type);
                } else if ("null".equals(strVal)) {
                    object = new KDLNull(type);
                } else {
                    object = new KDLString(strVal, type);
                }
            } else {
                object = new KDLString(strVal, type);
            }
        } else {
            throw new KDLParseException(String.format("Unexpected character: '%s'", (char) c));
        }

        if (object instanceof KDLString) {
            c = context.peek();
            if (c == '=') {
                if (type.isPresent()) {
                    throw new KDLParseException("Illegal type annotation before property, annotations should " +
                            "follow the '=' and precede the value");
                }

                context.read();
                final KDLValue<?> value = parseValue(context);
                return new KDLProperty(((KDLString) object).getValue(), value);
            } else if (isBare) {
                throw new KDLParseException(String.format("Arguments may not be bare: '%s'", ((KDLString) object).getValue()));
            } else {
                return object;
            }
        } else {
            return object;
        }
    }

    KDLDocument parseChild(KDLParseContext context) throws IOException {
        int c = context.read();
        if (c != '{') {
            throw new KDLInternalException(String.format("Expected '{' but found '%s'", (char) c));
        }

        final KDLDocument document = parseDocument(context, false);

        switch (consumeWhitespaceAndLinespace(context)) {
            case END_NODE:
                throw new KDLInternalException("Got unexpected END_NODE");
            case SKIP_NEXT:
                throw new KDLParseException("Trailing skip markers are not allowed");
            default:
                //Fall through
        }

        c = context.read();
        if (c != '}') {
            throw new KDLParseException("No closing brace found for child");
        }

        return document;
    }

    Optional<String> parseTypeIfPresent(KDLParseContext context) throws IOException {
        Optional<String> type = Optional.empty();
        int c = context.peek();
        if (c == '(') {
            context.read();
            type = Optional.of(parseIdentifier(context));
            c = context.read();
            if (c != ')') {
                throw new KDLParseException("Un-terminated type annotation, missing closing paren.");
            }
        }

        return type;
    }

    KDLValue<?> parseValue(KDLParseContext context) throws IOException {
        final Optional<String> type = parseTypeIfPresent(context);
        int c = context.peek();
        if (c == '"') {
            return new KDLString(parseEscapedString(context), type);
        } else if (c == 'r') {
            return new KDLString(parseRawString(context), type);
        } else if (isValidNumericStart(c)) {
            return parseNumber(context, type);
        } else {
            final StringBuilder stringBuilder = new StringBuilder();

            while (isLiteralChar(c)) {
                context.read();
                stringBuilder.appendCodePoint(c);
                c = context.peek();
            }

            final String strVal = stringBuilder.toString();
            switch (strVal) {
                case "true":
                    return new KDLBoolean(true, type);
                case "false":
                    return new KDLBoolean(false, type);
                case "null":
                    return new KDLNull(type);
                default:
                    throw new KDLParseException(String.format("Unknown literal in property value: '%s' Expected 'true', 'false', or 'null'", strVal));
            }
        }
    }

    KDLNumber parseNumber(KDLParseContext context, Optional<String> type) throws IOException {
        final int radix;
        Predicate<Integer> legalChars = null;

        int c = context.peek();
        if (c == '0') {
            context.read();
            c = context.peek();
            if (c == 'x') {
                context.read();
                radix = 16;
                legalChars = CharClasses::isValidHexChar;
            } else if (c == 'o') {
                context.read();
                radix = 8;
                legalChars = CharClasses::isValidOctalChar;
            } else if (c == 'b') {
                context.read();
                radix = 2;
                legalChars = CharClasses::isValidBinaryChar;
            } else {
                context.unread('0');
                radix = 10;
            }
        } else {
            radix = 10;
        }

        if (radix == 10) {
            return parseDecimalNumber(context, type);
        } else {
            return parseNonDecimalNumber(context, legalChars, radix, type);
        }
    }

    KDLNumber parseNonDecimalNumber(KDLParseContext context, Predicate<Integer> legalChars, int radix, Optional<String> type) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();

        int c = context.peek();
        if (c == '_') {
            throw new KDLParseException("The first character after radix indicator must not be '_'");
        }

        while (legalChars.test(c) || c == '_') {
            context.read();
            if (c != '_') {
                stringBuilder.appendCodePoint(c);
            }
            c = context.peek();
        }

        final String str = stringBuilder.toString();
        if (str.isEmpty()) {
            throw new KDLParseException("Must include at least one digit following radix marker");
        }

        return KDLNumber.from(new BigInteger(str, radix), radix, type);
    }

    // Unfortunately, in order to match the grammar we have to do a lot of parsing ourselves here
    KDLNumber parseDecimalNumber(KDLParseContext context, Optional<String> type) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();

        boolean inFraction = false;
        boolean inExponent = false;
        boolean signLegal = true;
        int c = context.peek();
        if (c == '_' || c == 'E' || c == 'e') {
            throw new KDLParseException(String.format("Decimal numbers may not begin with an '%s' character", (char) c));
        } else if (c == '+' || c == '-') {
            context.read();
            int sign = c;
            c = context.peek();
            if (c == '_') {
                throw new KDLParseException("Numbers may not begin with an '_' character after sign");
            } else {
                stringBuilder.appendCodePoint(sign);
                signLegal = false;
            }
        }

        c = context.peek();
        while (isValidDecimalChar(c) || c == 'e' || c == 'E' || c == '_' || c == '.' || c == '-' || c == '+') {
            context.read();
            if (c == '.') {
                if (inFraction || inExponent) {
                    throw new KDLParseException("The '.' character is not allowed in the fraction or exponent of a decimal");
                }

                if (!isValidDecimalChar(context.peek())) {
                    throw new KDLParseException("The character following '.' in a decimal number must be a decimal digit");
                }

                inFraction = true;
                signLegal = false;
                stringBuilder.appendCodePoint(c);
            } else if (c == 'e' || c == 'E') {
                if (inExponent) {
                    throw new KDLParseException(String.format("Found '%s' in exponent", (char) c));
                }

                inExponent = true;
                inFraction = false;
                signLegal = true;
                stringBuilder.appendCodePoint(c);

                if (context.peek() == '_') {
                    throw new KDLParseException("Character following exponent marker must not be '_'");
                }
            } else if (c == '_') {
                if (inFraction) {
                    throw new KDLParseException("The '_' character is not allowed in the fraction portion of decimal");
                }
                signLegal = false;
            } else if (c == '+' || c == '-') {
                if (!signLegal) {
                    throw new KDLParseException(String.format("The sign character '%s' is not allowed here", (char) c));
                }

                signLegal = false;
                stringBuilder.appendCodePoint(c);
            } else {
                signLegal = false;
                stringBuilder.appendCodePoint(c);
            }

            c = context.peek();
        }

        final String val = stringBuilder.toString();
        try {
            return KDLNumber.from(new BigDecimal(val), type);
        } catch (NumberFormatException e) {
            throw new KDLInternalException(String.format("Couldn't parse pre-vetted input '%s' into a BigDecimal", val), e);
        }
    }

    String parseBareIdentifier(KDLParseContext context) throws IOException {
        int c = context.read();
        if (!isValidBareIdStart(c)) {
            throw new KDLParseException("Illegal character at start of bare identifier");
        } else if (c == EOF) {
            throw new KDLInternalException("EOF when a bare identifier expected");
        }

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.appendCodePoint(c);

        c = context.peek();
        while (isValidBareIdChar(c) && c != EOF) {
            stringBuilder.appendCodePoint(context.read());
            c = context.peek();
        }

        return stringBuilder.toString();
    }

    String parseEscapedString(KDLParseContext context) throws IOException {
        int c = context.read();
        if (c != '"') {
            throw new KDLInternalException("No quote at the beginning of escaped string");
        }

        final StringBuilder stringBuilder = new StringBuilder();
        boolean inEscape = false;
        while (true) {
            c = context.read();
            if (!inEscape && c == '\\') {
                inEscape = true;
            } else if (c == '"' && !inEscape) {
                return stringBuilder.toString();
            } else if (inEscape) {
                stringBuilder.appendCodePoint(getEscaped(c, context));
                inEscape = false;
            } else if (c == EOF) {
                throw new KDLParseException("EOF while reading an escaped string");
            } else {
                stringBuilder.appendCodePoint(c);
            }
        }
    }

    int getEscaped(int c, KDLParseContext context) throws IOException {
        switch (c) {
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case '\\':
                return '\\';
            case '/':
                return '/';
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

                c = context.read();
                while (c != '}') {
                    if (c == EOF) {
                        throw new KDLParseException("Reached EOF while reading unicode escape sequence");
                    } else if (!isValidHexChar(c)) {
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

    String parseRawString(KDLParseContext context) throws IOException {
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
                } else {
                    throw new KDLParseException("Too many # characters when closing raw string");
                }
            } else if (c == EOF) {
                throw new KDLParseException("EOF while reading raw string");
            } else {
                stringBuilder.appendCodePoint(c);
            }
        }
    }

    SlashAction getSlashAction(KDLParseContext context, boolean escaped) throws IOException {
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
                if (escaped) {
                    return SlashAction.NOTHING;
                } else {
                    return SlashAction.END_NODE;
                }
            default:
                throw new KDLParseException(String.format("Unexpected character: '%s'", (char) c));
        }
    }

    void consumeAfterNode(KDLParseContext context) throws IOException {
        int c = context.peek();
        while (c == ';' || isUnicodeWhitespace(c)) {
            context.read();
            c = context.peek();
        }
    }

    WhitespaceResult consumeWhitespaceAndBlockComments(KDLParseContext context) throws IOException {
        boolean skipping = false;
        boolean foundWhitespace = false;
        boolean inLineEscape = false;
        boolean foundSemicolon = false;
        int c = context.peek();
        while (c == '/' || c == '\\' || c == ';' || c == '\uFEFF' || isUnicodeWhitespace(c) || isUnicodeLinespace(c)) {
            if (c == '/') {
                switch (getSlashAction(context, inLineEscape)) {
                    case END_NODE:
                        if (inLineEscape) {
                            foundWhitespace = true;
                            inLineEscape = false;
                            break;
                        }
                        return WhitespaceResult.END_NODE;

                    case SKIP_NEXT:
                        if (inLineEscape) {
                            throw new KDLParseException("Found skip marker after line escape");
                        }

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
            } else if (c == ';') {
                context.read();
                foundSemicolon = true;
            } else if (c == '\\') {
                context.read();
                inLineEscape = true;
            } else if (isUnicodeLinespace(c)) {
                if (inLineEscape) {
                    context.read();
                    if (c == '\r') {
                        c = context.peek();
                        if (c == '\n') {
                            context.read();
                        }
                    }

                    inLineEscape = false;
                    foundWhitespace = true;
                } else {
                    break;
                }
            } else {
                context.read();
                foundWhitespace = true;
            }

            c = context.peek();
        }

        if (skipping) {
            return WhitespaceResult.SKIP_NEXT;
        } else if (foundSemicolon) {
            return WhitespaceResult.END_NODE;
        } else if (foundWhitespace) {
            return WhitespaceResult.NODE_SPACE;
        } else {
            return WhitespaceResult.NO_WHITESPACE;
        }
    }

    void consumeLineComment(KDLParseContext context) throws IOException {
        int c = context.peek();
        while (!isUnicodeLinespace(c) && c != EOF) {
            context.read();
            if (c == '\r') {
                c = context.peek();
                if (c == '\n') {
                    context.read();
                }
                return;
            }
            c = context.peek();
        }
    }

    void consumeBlockComment(KDLParseContext context) throws IOException {
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
                c = context.peek();
                if (c == '/') {
                    context.read();
                    return;
                }
            }
        }
    }

    WhitespaceResult consumeWhitespaceAndLinespace(KDLParseContext context) throws IOException {
        boolean skipNext = false;
        boolean foundWhitespace = false;
        boolean inEscape = false;
        while (true) {
            int c = context.peek();
            boolean isLinespace = isUnicodeLinespace(c);
            while (isUnicodeWhitespace(c) || isLinespace) {
                foundWhitespace = true;
                if (isLinespace && skipNext) {
                    throw new KDLParseException("Unexpected newline after skip marker");
                }

                if (isLinespace && inEscape) {
                    inEscape = false;
                }

                context.read();
                c = context.peek();
                isLinespace = isUnicodeLinespace(c);
            }

            if (c == '/') {
                switch (getSlashAction(context, inEscape)) {
                    case END_NODE:
                    case NOTHING:
                        foundWhitespace = true;
                        break;
                    case SKIP_NEXT:
                        foundWhitespace = true;
                        skipNext = true;
                }
            } else if (c == '\\') {
                context.read();
                foundWhitespace = true;
                inEscape = true;
            } else if (c == EOF) {
                if (skipNext) {
                    throw new KDLParseException("Unexpected EOF after skip marker");
                } else if (inEscape) {
                    throw new KDLParseException("Unexpected EOF after line escape");
                } else {
                    return WhitespaceResult.END_NODE;
                }
            } else {
                if (inEscape) {
                    throw new KDLParseException("Expected newline or line comment following escape");
                }

                if (skipNext) {
                    return WhitespaceResult.SKIP_NEXT;
                } else if (foundWhitespace) {
                    return WhitespaceResult.NODE_SPACE;
                } else {
                    return WhitespaceResult.NO_WHITESPACE;
                }
            }
        }
    }
}
