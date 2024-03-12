package kdl.parse.lexer.token;

import jakarta.annotation.Nonnull;
import kdl.parse.lexer.Token;

public interface Parentheses extends Token {

	class OpeningParentheses implements Parentheses {
		private OpeningParentheses() {
		}

		@Override
		@Nonnull
		public String value() {
			return "(";
		}

		@Override
		public String toString() {
			return "OpeningParentheses";
		}
	}

	class ClosingParentheses implements Parentheses {
		private ClosingParentheses() {
		}

		@Override
		@Nonnull
		public String value() {
			return ")";
		}

		@Override
		public String toString() {
			return "ClosingParentheses";
		}
	}

	OpeningParentheses OPENING_PARENTHESES = new OpeningParentheses();
	ClosingParentheses CLOSING_PARENTHESES = new ClosingParentheses();
}
