package dev.kdl.parse.lexer.helper;

import dev.kdl.parse.lexer.token.ByteOrderMark;

import static dev.kdl.parse.lexer.helper.KdlCharHelper.CR;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.LF;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.isUnicodeScalarValue;

public class Kdl1CharHelper {
	public static boolean isNewline(int c) {
		return switch (c) {
			case CR, // Carriage Return
				 LF, // Line Feed
				 0x0085, // Next Line
				 0x000C, // Form Feed
				 0x2028, // Line Separator
				 0x2029 // Paragraph Separator
				-> true;
			default -> false;
		};
	}

	public static boolean isWhitespace(int c) {
		return switch (c) {
			case ByteOrderMark.VALUE,
				 0x0009, // Character Tabulation
				 0x0020, // Space
				 0x00A0, // No-Break Space
				 0x1680, // Ogham Space Mark
				 0x2000, // En Quad
				 0x2001, // Em Quad
				 0x2002, // En Space
				 0x2003, // Em Space
				 0x2004, // Three-Per-Em Space
				 0x2005, // Four-Per-Em Space
				 0x2006, // Six-Per-Em Space
				 0x2007, // Figure Space
				 0x2008, // Punctuation Space
				 0x2009, // Thin Space
				 0x200A, // Hair Space
				 0x202F, // Narrow No-Break Space
				 0x205F, // Medium Mathematical Space
				 0x3000 // Ideographic Space
				-> true;
			default -> false;
		};
	}

	public static boolean isIdentifierChar(int c) {
		return isUnicodeScalarValue(c) && !isWhitespace(c) && !isNewline(c) && !isSpecialCharacter(c);
	}

	public static boolean isSpecialCharacter(int c) {
		return switch (c) {
			case '\\', '/', '(', ')', '{', '}', '<', '>', ';', '[', ']', '=', ',', '"' -> true;
			default -> false;
		};
	}
}
