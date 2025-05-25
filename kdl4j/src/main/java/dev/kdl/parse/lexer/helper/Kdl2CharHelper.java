package dev.kdl.parse.lexer.helper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.kdl.parse.lexer.helper.KdlCharHelper.CR;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.LF;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.isDecimalDigit;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.isUnicodeScalarValue;

/**
 * A helper class for different predicates on codepoints. Used by KDL 2.0 parser and printer.
 */
public class Kdl2CharHelper {
	/**
	 * Checks if a codepoint is a newline character
	 *
	 * @param c a codepoint
	 * @return true if c is a newline character, false otherwise
	 */
	public static boolean isNewline(int c) {
		return switch (c) {
			case CR, // Carriage Return
				 LF, // Line Feed
				 0x0085, // Next Line
				 0x000B, // Vertical tab
				 0x000C, // Form Feed
				 0x2028, // Line Separator
				 0x2029 // Paragraph Separator
				-> true;
			default -> false;
		};
	}

	/**
	 * Checks if a codepoint is a whitespace character
	 *
	 * @param c a codepoint
	 * @return true if c is a whitespace character, false otherwise
	 */
	public static boolean isWhitespace(int c) {
		return switch (c) {
			case 0x0009, // Character Tabulation
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

	/**
	 * Checks if a codepoint is an identifier character
	 *
	 * @param c a codepoint
	 * @return true if c is an identifier character, false otherwise
	 */
	public static boolean isIdentifierChar(int c) {
		return isUnicodeScalarValue(c) && !isWhitespace(c) && !isNewline(c) && !isSpecialCharacter(c);
	}

	private static boolean isSpecialCharacter(int c) {
		return switch (c) {
			case '\\', '/', '(', ')', '{', '}', ';', '[', ']', '"', '#', '=' -> true;
			default -> false;
		};
	}

	/**
	 * Checks if a codepoint is an unambiguous identifier character
	 *
	 * @param c a codepoint
	 * @return true if c is an unambiguous identifier character, false otherwise
	 */
	public static boolean isUnambiguousIdentifierChar(int c) {
		return c != '-' && c != '+' && c != '.' && !isDecimalDigit(c) && isIdentifierChar(c);
	}

	/**
	 * Checks if an identifier is a disallowed value.
	 *
	 * @param value the value of an identifier
	 * @return true if value is a disallowed value, false otherwise
	 */
	public static boolean isDisallowedIdentifier(String value) {
		return DISALLOWED_IDENTIFIERS.contains(value);
	}

	private static final Set<String> DISALLOWED_IDENTIFIERS = new HashSet<>(List.of("true", "false", "null", "inf", "-inf", "nan"));
}
