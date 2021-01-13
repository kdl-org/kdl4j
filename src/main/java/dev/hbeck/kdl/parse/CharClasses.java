package dev.hbeck.kdl.parse;

import java.util.Optional;

/**
 * Various functions used during parsing and printing to check character membership in various character classes.
 * <p>
 * Also contains functions for transforming characters into their escape sequences.
 */
public class CharClasses {

    /**
     * Check if the character is valid at the beginning of a numeric value
     *
     * @param c the character to check
     * @return true if the character is valid, false otherwise
     */
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

    /**
     * Check if the character is valid in a bare identifier after the first character
     *
     * @param c the character to check
     * @return true if the character is valid, false otherwise
     */
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

    /**
     * Check if the character is valid in a bare identifier as the first character
     *
     * @param c the character to check
     * @return true if the character is valid, false otherwise
     */
    public static boolean isValidBareIdStart(int c) {
        return !isValidDecimalChar(c) && isValidBareIdChar(c);
    }

    /**
     * Check if a string is a valid bare identifier
     *
     * @param string the string to check
     * @return true if the string is a valid bare id, false otherwise
     */
    public static boolean isValidBareId(String string) {
        if (string.isEmpty()) {
            return false;
        }

        final boolean validBareIdStart = isValidBareIdStart(string.charAt(0));
        if (string.length() == 1 || !validBareIdStart) {
            return validBareIdStart;
        }

        for (int i = 0; i < string.length(); i++) {
            if (!isValidBareIdChar(string.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if the character is a valid decimal digit
     *
     * @param c the character to check
     * @return true if the character is valid, false otherwise
     */
    public static boolean isValidDecimalChar(int c) {
        return '0' <= c && c <= '9';
    }

    /**
     * Check if the character is a valid hexadecimal digit
     *
     * @param c the character to check
     * @return true if the character is valid, false otherwise
     */
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

    /**
     * Check if the character is a valid octal digit
     *
     * @param c the character to check
     * @return true if the character is valid, false otherwise
     */
    public static boolean isValidOctalChar(int c) {
        return '0' <= c && c <= '7';
    }

    /**
     * Check if the character is a valid binary digit
     *
     * @param c the character to check
     * @return true if the character is valid, false otherwise
     */
    public static boolean isValidBinaryChar(int c) {
        return c == '0' || c == '1';
    }

    /**
     * Check if the character is contained in one of the three literal values: true, false, and null
     *
     * @param c the character to check
     * @return true if the character appears in a literal, false otherwise
     */
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

    /**
     * Check if the character is a unicode newline of any kind
     *
     * @param c the character to check
     * @return true if the character is a unicode newline, false otherwise
     */
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

    /**
     * Check if the character is unicode whitespace of any kind
     *
     * @param c the character to check
     * @return true if the character is unicode whitespace, false otherwise
     */
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

    /**
     * Check if the character is an ASCII character that can be printed unescaped
     *
     * @param c the character to check
     * @return true if the character is printable unescaped, false otherwise
     */
    public static boolean isPrintableAscii(int c) {
        return ' ' <= c && c <= '~';
    }

    public static boolean isNonAscii(int c) {
        return c > 127;
    }

    public static boolean mustEscape(int c) {
        return c == '\\' || c == '"' || c == '/';
    }

    private static final Optional<String> ESC_BACKSLASH = Optional.of("\\\\");
    private static final Optional<String> ESC_BACKSPACE = Optional.of("\\b");
    private static final Optional<String> ESC_NEWLINE = Optional.of("\\n");
    private static final Optional<String> ESC_FORM_FEED = Optional.of("\\f");
    private static final Optional<String> ESC_FORWARD_SLASH = Optional.of("\\/");
    private static final Optional<String> ESC_TAB = Optional.of("\\t");
    private static final Optional<String> ESC_CR = Optional.of("\\r");
    private static final Optional<String> ESC_QUOTE = Optional.of("\\\"");

    /**
     * Get the escape sequence for characters from the ASCII character set
     *
     * @param c the character to check
     * @return An Optional wrapping the escape sequence string if the character needs to be escaped, or false otherwise
     */
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

    public static boolean isCommonEscape(int c) {
        switch (c) {
            case '\\':
            case '\b':
            case '\n':
            case '\f':
            case '/':
            case '\t':
            case '\r':
            case '"':
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the escape sequence for any character
     *
     * @param c the character to check
     * @return The escape sequence string
     */
    public static String getEscapeIncludingUnicode(int c) {
        return getCommonEscape(c).orElseGet(() -> String.format("\\u{%x}", c));
    }
}
