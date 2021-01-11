package dev.hbeck.kdl.parse;

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
}
