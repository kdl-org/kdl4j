package dev.kdl.parse.lexer.helper;

public class KdlCharHelper {
	public static final int LF = 0x000A;
	public static final int CR = 0x000D;

	public static boolean isUnicodeScalarValue(int c) {
		return c >= 0 && c <= 0xD7FF || c >= 0xE000 && c <= 0x10FFFF;
	}

	public static boolean isDecimalDigit(int c) {
		return c >= '0' && c <= '9';
	}

	public static boolean isHexadecimalDigit(int c) {
		return isDecimalDigit(c) || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
	}

	public static boolean isOctalDigit(int c) {
		return c >= '0' && c <= '7';
	}

	public static boolean isBinaryDigit(int c) {
		return c == '0' || c == '1';
	}

	public static boolean isSign(int c) {
		return c == '-' || c == '+';
	}
}
