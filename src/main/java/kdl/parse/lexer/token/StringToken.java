package kdl.parse.lexer.token;

import jakarta.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import kdl.parse.lexer.Token;

import static kdl.parse.lexer.token.EqualsSign.isEqualsSign;
import static kdl.parse.lexer.token.Newline.isNewline;
import static kdl.parse.lexer.token.Number.isDigit;
import static kdl.parse.lexer.token.Whitespace.isWhitespace;

public interface StringToken extends Token {
	class IdentifierString implements StringToken {
		public IdentifierString(String value) {
			this.value = value;
		}

		@Override
		@Nonnull
		public String value() {
			return value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			var that = (IdentifierString) o;
			return Objects.equals(value, that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public String toString() {
			return "IdentifierString(" + value + ")";
		}

		private final String value;

		public static boolean isIdentifierChar(int c) {
			return isUnicodeScalarValue(c) && !isWhitespace(c) && !isNewline(c) && !isSpecialCharacter(c) && !isEqualsSign(c);
		}

		public static boolean isUnicodeScalarValue(int c) {
			return c >= 0 && c <= 0xD7FF || c >= 0xE000 && c <= 0x10FFFF;
		}

		public static boolean isSpecialCharacter(int c) {
			switch (c) {
				case '\\':
				case '/':
				case '(':
				case ')':
				case '{':
				case '}':
				case ';':
				case '[':
				case ']':
				case '"':
				case '#':
					return true;
				default:
					return false;
			}
		}

		public static boolean isUnambiguousIdentifierChar(int c) {
			return c != '-' && c != '+' && c != '.' && !isDigit(c) && isIdentifierChar(c);
		}

		public static boolean isDisallowedIdentifier(String value) {
			return DISALLOWED_IDENTIFIERS.contains(value);
		}

		public static final Set<String> DISALLOWED_IDENTIFIERS = new HashSet<>(List.of("true", "false", "null", "inf", "-inf", "nan"));
	}

	class QuotedString implements StringToken {
		public QuotedString(String value) {
			this.value = value;
		}

		@Override
		@Nonnull
		public String value() {
			return value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			QuotedString that = (QuotedString) o;
			return Objects.equals(value, that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public String toString() {
			return "QuotedString(" + value + ')';
		}

		private final String value;
	}
}
