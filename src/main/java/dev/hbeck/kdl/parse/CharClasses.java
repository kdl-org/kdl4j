package dev.hbeck.kdl.parse;

import java.util.Optional;

public class CharClasses {

    public static boolean isValidNumericStart(int c) {
        switch (c) {
            case '+':
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return true;
            default:
                return false;
        }
    }


    public static boolean isValidBareIdChar(int c) {
        switch (c) {
            case '\n':
            case '\u000C':
            case '\r':
            case '\u0085':
            case '\u2028':
            case '\u2029':
            case '\\':
            case '{':
            case '}':
            case '<':
            case '>':
            case ';':
            case '[':
            case ']':
            case '=':
            case ',':
            case '"':
            case '\u0009':
            case '\u0020':
            case '\u00A0':
            case '\u1680':
            case '\u2000':
            case '\u2001':
            case '\u2002':
            case '\u2003':
            case '\u2004':
            case '\u2005':
            case '\u2006':
            case '\u2007':
            case '\u2008':
            case '\u2009':
            case '\u200A':
            case '\u202F':
            case '\u205F':
            case '\u3000':
                return false;
            default:
                return true;
        }
    }

    public static boolean isValidBareIdStart(int c) {
        return !isValidDecimalChar(c) && isValidBareIdChar(c);
    }

    public static boolean isValidDecimalChar(int c) {
        return '0' <= c && c <= '9';
    }

    public static boolean isValidHexChar(int c) {
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'A':
            case 'a':
            case 'B':
            case 'b':
            case 'C':
            case 'c':
            case 'D':
            case 'd':
            case 'E':
            case 'e':
            case 'F':
            case 'f':
                return true;
            default:
                return false;
        }
    }

    public static boolean isValidOctalChar(int c) {
        return '0' <= c && c <= '7';
    }

    public static boolean isValidBinaryChar(int c) {
        return c == '0' || c == '1';
    }

    public static boolean isLiteralChar(int c) {
        switch (c) {
            case 't':
            case 'r':
            case 'u':
            case 'e':
            case 'n':
            case 'l':
            case 'f':
            case 'a':
            case 's':
                return true;
            default:
                return false;
        }
    }

    public static boolean isUnicodeLinespace(int c) {
        switch (c) {
            case '\r':
            case '\n':
            case '\u0085':
            case '\u000C':
            case '\u2028':
            case '\u2029':
                return true;
            default:
                return false;
        }
    }

    public static boolean isUnicodeWhitespace(int c) {
        switch (c) {
            case '\u0009':
            case '\u0020':
            case '\u00A0':
            case '\u1680':
            case '\u2000':
            case '\u2001':
            case '\u2002':
            case '\u2003':
            case '\u2004':
            case '\u2005':
            case '\u2006':
            case '\u2007':
            case '\u2008':
            case '\u2009':
            case '\u200A':
            case '\u202F':
            case '\u205F':
            case '\u3000':
                return true;
            default:
                return false;
        }
    }

    public static boolean isPrintableAscii(int c) {
        return ' ' <= c && c <= '~';
    }

    private static final Optional<String> ESC_BACKSLASH = Optional.of("\\\\");
    private static final Optional<String> ESC_BACKSPACE = Optional.of("\\b");
    private static final Optional<String> ESC_NEWLINE = Optional.of("\\n");
    private static final Optional<String> ESC_FORM_FEED = Optional.of("\\f");
    private static final Optional<String> ESC_FORWARD_SLASH = Optional.of("\\/");
    private static final Optional<String> ESC_TAB = Optional.of("\\t");
    private static final Optional<String> ESC_CR = Optional.of("\\r");
    private static final Optional<String> ESC_QUOTE = Optional.of("\\\"");

    public static Optional<String> getCommonEscape(int c) {
        switch (c) {
            case '\\':
                return ESC_BACKSLASH;
            case '\b':
                return ESC_BACKSPACE;
            case '\n':
                return ESC_NEWLINE;
            case '\f':
                return ESC_FORM_FEED;
            case '/':
                return ESC_FORWARD_SLASH;
            case '\t':
                return ESC_TAB;
            case '\r':
                return ESC_CR;
            case '"':
                return ESC_QUOTE;
            default:
                return Optional.empty();
        }
    }

    public static String getEscapeIncludingUnicode(int c) {
        return getCommonEscape(c).orElseGet(() -> String.format("\\u{%x}", c));
    }
}
