package dev.kdl.parse.lexer.helper;

/**
 * A helper class for different predicates on codepoints.
 */
public class KdlCharHelper {
	/**
	 * The codepoint for the line feed character.
	 */
	public static final int LF = 0x000A;

	/**
	 * The codepoint for the carriage return character.
	 */
	public static final int CR = 0x000D;

	/**
	 * Checks if a codepoint is a valid Unicode codepoint
	 *
	 * @param c a codepoint
	 * @return true if c is a valid Unicode codepoint, false otherwise
	 */
	public static boolean isUnicodeScalarValue(int c) {
		return c >= 0 && c <= 0xD7FF || c >= 0xE000 && c <= 0x10FFFF;
	}

	/**
	 * Checks if a codepoint is a decimal digit character
	 *
	 * @param c a codepoint
	 * @return true if c is a decimal digit character, false otherwise
	 */
	public static boolean isDecimalDigit(int c) {
		return c >= '0' && c <= '9';
	}

	/**
	 * Checks if a codepoint is a hexadecimal digit character
	 *
	 * @param c a codepoint
	 * @return true if c is a hexadecimal digit character, false otherwise
	 */
	public static boolean isHexadecimalDigit(int c) {
		return isDecimalDigit(c) || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
	}

	/**
	 * Checks if a codepoint is an octal digit character
	 *
	 * @param c a codepoint
	 * @return true if c is an octal digit character, false otherwise
	 */
	public static boolean isOctalDigit(int c) {
		return c >= '0' && c <= '7';
	}

	/**
	 * Checks if a codepoint is a binary digit character
	 *
	 * @param c a codepoint
	 * @return true if c is a binary digit character, false otherwise
	 */
	public static boolean isBinaryDigit(int c) {
		return c == '0' || c == '1';
	}

	/**
	 * Checks if a codepoint is a sign character
	 *
	 * @param c a codepoint
	 * @return true if c is a sign character, false otherwise
	 */
	public static boolean isSign(int c) {
		return c == '-' || c == '+';
	}
}
