package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.kdl.parse.lexer.token.Newline.isNewline;
import static dev.kdl.parse.lexer.token.NodeSpace.isWhitespace;
import static dev.kdl.parse.lexer.token.Number.isDecimalDigit;

public interface StringToken extends Token {
	record IdentifierString(@Nonnull String value, @Nonnull Span span) implements StringToken {
		public static boolean isIdentifierChar(int c) {
			return isUnicodeScalarValue(c) && !isWhitespace(c) && !isNewline(c) && !isSpecialCharacter(c) && c != '=';
		}

		public static boolean isUnicodeScalarValue(int c) {
			return c >= 0 && c <= 0xD7FF || c >= 0xE000 && c <= 0x10FFFF;
		}

		public static boolean isSpecialCharacter(int c) {
			return switch (c) {
				case '\\', '/', '(', ')', '{', '}', ';', '[', ']', '"', '#' -> true;
				default -> false;
			};
		}

		public static boolean isUnambiguousIdentifierChar(int c) {
			return c != '-' && c != '+' && c != '.' && !isDecimalDigit(c) && isIdentifierChar(c);
		}

		public static boolean isDisallowedIdentifier(String value) {
			return DISALLOWED_IDENTIFIERS.contains(value);
		}

		public static final Set<String> DISALLOWED_IDENTIFIERS = new HashSet<>(List.of("true", "false", "null", "inf", "-inf", "nan"));

	}

	record QuotedString(@Nonnull String value, @Nonnull Span span) implements StringToken {
	}
}
