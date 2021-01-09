package dev.hbeck.kdl.parse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import dev.hbeck.kdl.objects.KDLBoolean;
import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLIdentifier;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLNull;
import dev.hbeck.kdl.objects.KDLNumber;
import dev.hbeck.kdl.objects.KDLObject;
import dev.hbeck.kdl.objects.KDLProperty;
import dev.hbeck.kdl.objects.KDLString;
import dev.hbeck.kdl.objects.KDLValue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KDLParserV2 {

    public static final int EOF = -1;
    public static final int MAX_UNICODE = 0x10FFF;

    public KDLDocument parse(Reader reader) throws IOException {
        final KDLParseContext context = new KDLParseContext(reader);

        try {
            return parseDocument(context);
        } catch (KDLParseException e) {
            final String message = String.format("%s\n%s", e.getMessage(), context.getCurrentPosition());
            throw new KDLParseException(message, e);
        }
    }

    public KDLDocument parse(String string) {
        final StringReader reader = new StringReader(string);
        try {
            return parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private KDLDocument parseDocument(KDLParseContext context) throws IOException {
        consumeWhitespaceAndLinespace(context);
        int c = context.read();
        if (c == EOF) {
            return new KDLDocument(ImmutableList.of());
        }

        context.unread(c);
        final ImmutableList.Builder<KDLNode> listBuilder = ImmutableList.builder();
        while (true) {
            parseNode(context).ifPresent(listBuilder::add);
            consumeWhitespaceAndLinespace(context);

            c = context.read();
            if (c == EOF) {
                return new KDLDocument(listBuilder.build());
            } else {
                context.unread(c);
            }
        }
    }

    private SlashAction handleSlash(KDLParseContext reader) {

    }











    private Optional<KDLNode> parseNode(KDLParseContext context) throws IOException {
        boolean skipNode = false;
        int c = context.read();
        if (c == '/') {
            c = context.read();
            if (c == '/') {
                consumeLineComment(context);
            } else if (c == '-') {
                skipNode = true;
                consumeWhitespace(context);
            }
        } else {
            context.unread(c);
        }

        final KDLIdentifier identifier = parseIdentifier(context);
        if (!consumeWhitespace(context)) {
            c = context.read();
            if (!UNICODE_LINESPACE.contains(c) && c != EOF && c != '/') {
                throw new KDLParseException("");
            }

            if (c == '/') {
                context.unread(c);
            } else if (skipNode) {
                return Optional.empty();
            } else {
                return Optional.of(new KDLNode(identifier, ImmutableMap.of(), ImmutableList.of(), Optional.empty()));
            }
        }

        final ImmutableList.Builder<KDLValue> args = ImmutableList.builder();
        final ImmutableMap.Builder<KDLIdentifier, KDLValue> properties = ImmutableMap.builder();

        c = context.read();
        boolean skipNext = false;
        while (c != '{' && !UNICODE_LINESPACE.contains(c)) {
            consumeWhitespace(context);
            if (c == '/') {
                c = context.read();
                if (c == '/') {
                    consumeLineComment(context);
                } else if (c == '*') {
                    consumeBlockComment(context);
                } else if (c == '-') {
                    skipNext = true;
                    consumeWhitespace(context);
                }
            } else {
                skipNext = false;
                context.unread(c);
            }

            final KDLObject kdlObject = parsePropertyOrArg(context);
            if (!skipNext) {
                if (kdlObject instanceof KDLValue) {
                    args.add((KDLValue) kdlObject);
                } else {
                    final KDLProperty property = (KDLProperty) kdlObject;
                    properties.put(property.getKey(), property.getValue());
                }
            }

            c = context.read();
        }

        Optional<KDLDocument> child = Optional.empty();
        if (c == '{') {
            consumeWhitespaceAndLinespace(context);

        }

        if (skipNode) {

        } else {

        }
    }

    private static final Set<Integer> INVALID_BARE_ID_CHARS = Stream.of('\n', '\u000C', '\r', '\u0085', '\u2028', '\u2029',
            '\\', '{', '}', '<', '>', ';', '[', ']', '=', ',', '"', '#', '\u0009', '\u0020', '\u00A0', '\u1680', '\u2000',
            '\u2001', '\u2002', '\u2003', '\u2004', '\u2005', '\u2006', '\u2007', '\u2008', '\u2009', '\u200A', '\u202F',
            '\u205F', '\u3000')
            .map(character -> (int) character)
            .collect(Collectors.toSet());

    private static final Set<Integer> INVALID_BARE_ID_START_CHARS = ImmutableSet.<Integer>builder().addAll(INVALID_BARE_ID_CHARS)
            .addAll(Stream.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9').map(character -> (int) character).collect(Collectors.toSet()))
            .build();

    private KDLIdentifier parseIdentifier(KDLParseContext context) throws IOException {
        int c = context.read();
        if (c == 'r') {
            c = context.read();
            context.unread(c);
            context.unread('r');
            if (c == '"' || c == '#') {
                final KDLString kdlString = parseString(context);
                return new KDLIdentifier(kdlString.getValue());
            }
            // else fall through to bare ID
        } else if (c == '"') {
            context.unread(c);
            final KDLString kdlString = parseString(context);
            return new KDLIdentifier(kdlString.getValue());
        } else if (INVALID_BARE_ID_START_CHARS.contains(c)) {
            throw new KDLParseException("");
        }

        // It's a bare id
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.appendCodePoint(c);
        c = context.read();
        while (!INVALID_BARE_ID_CHARS.contains(c)) {
            stringBuilder.appendCodePoint(c);
            c = context.read();
        }

        return new KDLIdentifier(stringBuilder.toString());

    }

    private KDLObject parsePropertyOrArg(KDLParseContext context) {
        return null;
    }

    private KDLObject parseValueOrQuotedProperty(KDLParseContext context) throws IOException {
        int c = context.read();
        if (c == '"' || c == 'r') {
            context.unread(c);
            final KDLString string = parseString(context);

            c = context.read();
            if (c == '=') {
                final KDLObject value = parseValueOrQuotedProperty(context);
                if (value instanceof KDLValue) {
                    return new KDLProperty(new KDLIdentifier(string.getValue()), (KDLValue) value);
                } else {
                    throw new KDLParseException("");
                }
            } else {
                context.unread(c);
                return string;
            }
        } else if (c == '0' || DECIMAL_CHARS.contains(c)) {
            context.unread(c);
            return parseNumber(context);
        } else if (c == 't' || c == 'f') {
            context.unread(c);
            return parseBoolean(context);
        } else if (c == 'n') {
            int u = context.read();
            int l = context.read();
            int ll = context.read();
            if (u == 'u' && l == 'l' && ll == 'l') {
                return KDLNull.INSTANCE;
            } else {
                throw new KDLParseException("");
            }
        } else {
            throw new KDLParseException("");
        }
    }

    private KDLBoolean parseBoolean(KDLParseContext context) throws IOException {
        int c = context.read();
        if (c == 't') {
            int r = context.read();
            int u = context.read();
            int e = context.read();
            if (r == 'r' && u == 'u' && e == 'e') {
                return KDLBoolean.TRUE;
            } else {
                throw new KDLParseException("");
            }
        } else if (c == 'f') {
            int a = context.read();
            int l = context.read();
            int s = context.read();
            int e = context.read();
            if (a == 'a' && l == 'l' && s == 's' && e == 'e') {
                return KDLBoolean.FALSE;
            } else {
                throw new KDLParseException("");
            }
        } else {
            throw new KDLParseException("");
        }
    }

    private KDLString parseString(KDLParseContext context) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        int c = context.read();
        if (c == '"') {
            boolean inEscape = false;
            while (true) {
                c = context.read();
                if (c == '\\') {
                    inEscape = true;
                } else if (c == '"' && !inEscape) {
                    return new KDLString(stringBuilder.toString());
                } else if (inEscape) {
                    stringBuilder.append((char) getEscaped(c, context));
                    inEscape = false;
                } else if (c == EOF) {
                    throw new KDLParseException("");
                } else {
                    stringBuilder.appendCodePoint(c);
                }
            }
        } else if (c == 'r') {
            int hashDepth = 0;
            c = context.read();
            while (c == '#') {
                hashDepth++;
                c = context.read();
            }

            if (c != '"') {
                throw new KDLParseException("Malformed raw string");
            }

            while (true) {
                c = context.read();
                if (c == '"') {
                    StringBuilder subStringBuilder = new StringBuilder();
                    subStringBuilder.append('"');
                    int hashDepthHere = 0;
                    while (true) {
                        c = context.read();
                        if (c == '#') {
                            hashDepthHere++;
                            subStringBuilder.append('#');
                        } else {
                            hashDepthHere = 0;
                            context.unread(c);
                            if (hashDepthHere < hashDepth) {
                                stringBuilder.append(subStringBuilder);
                                subStringBuilder = new StringBuilder();
                            } else if (hashDepth == hashDepthHere) {
                                return new KDLString(stringBuilder.toString());
                            } else {
                                throw new KDLParseException("Too many '#' when closing raw string");
                            }
                        }
                    }
                } else {
                    stringBuilder.appendCodePoint(c);
                }
            }
        }

        throw new KDLParseException("");
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
                    throw new KDLParseException(String.format("Unicode escape sequences must be between 1 and 6 characters in length. Got: '%s'", strCode))
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

    private KDLNumber parseNumber(KDLParseContext context) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        final int radix;
        final Set<Integer> legalChars;

        int c = context.read();
        if (c == '0') {
            c = context.read();
            if (c == 'x') {
                radix = 16;
                legalChars = HEX_CHARS;
            } else if (c == 'o') {
                radix = 8;
                legalChars = OCTAL_CHARS;
            } else if (c == 'b') {
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

        c = context.read();
        while (legalChars.contains(c) || c == '_') {
            if (c != '_') {
                stringBuilder.appendCodePoint(c);
            }
        }

        if (UNICODE_WHITESPACE.contains(c) || UNICODE_LINESPACE.contains(c) || c == '{') {
            context.unread(c);
        } else if (c == EOF) {
            // Do nothing
        } else {
            throw new KDLParseException(String.format("Illegal character in radix %d literal: '%s'", radix, (char) c));
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
                c = context.read();
                if (c == '*') {
                    consumeBlockComment(context);
                } else {
                    context.unread(c);
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

    private static final Set<Integer> UNICODE_LINESPACE = Stream.of('\r', '\n', '\u0085', '\u000C', '\u2028', '\u2029')
            .map(character -> (int) character)
            .collect(Collectors.toSet());

    private boolean consumeLinespace(KDLParseContext context) throws IOException {
        int c = context.read();
        boolean readAny = c != EOF;
        while (UNICODE_LINESPACE.contains(c)) {
            c = context.read();
        }

        if (c != EOF) {
            context.unread(c);
        }

        return readAny;
    }

    private static final Set<Integer> UNICODE_WHITESPACE =
            Stream.of('\u0009', '\u0020', '\u00A0', '\u1680', '\u2000', '\u2001', '\u2002', '\u2003', '\u2004', '\u2005',
                    '\u2006', '\u2007', '\u2008', '\u2009', '\u200A', '\u202F', '\u205F', '\u3000')
                    .map(character -> (int) character)
                    .collect(Collectors.toSet());

    private boolean consumeWhitespace(KDLParseContext context) throws IOException {
        int c = context.read();
        boolean readAny = false;
        while (UNICODE_WHITESPACE.contains(c) || c == '\\') {
            readAny = true;
            final boolean escape = c == '\\';
            c = context.read();
            if (escape) {
                if (c == '\r') {
                    c = context.read();
                    if (c != '\n') {
                        context.unread(c);
                    }
                } else if (!UNICODE_LINESPACE.contains(c)) {
                    throw new KDLParseException("");
                }
            }
        }

        if (c == '/') {
            c = context.read();
            if (c == '*') {
                consumeBlockComment(context);
                consumeWhitespace(context);
                readAny = true;
            } else {
                context.unread(c);
                context.unread('/');
            }
        } else if (c != EOF) {
            context.unread(c);
        }

        return readAny;
    }

    private boolean consumeWhitespaceAndLinespace(KDLParseContext context) throws IOException {
        int c = context.read();
        boolean readAny = false;
        while (UNICODE_WHITESPACE.contains(c) || UNICODE_LINESPACE.contains(c)) {
            readAny = true;
            c = context.read();
        }

        if (c != EOF) {
            context.unread(c);
        }

        return readAny;
    }
}
