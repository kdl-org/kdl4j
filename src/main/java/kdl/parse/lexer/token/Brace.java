package kdl.parse.lexer.token;

import jakarta.annotation.Nonnull;
import kdl.parse.lexer.Token;

public interface Brace extends Token {

	class OpeningBrace implements Brace {
		private OpeningBrace() {
		}

		@Override
		@Nonnull
		public String value() {
			return "(";
		}

		@Override
		public String toString() {
			return "OpeningBrace";
		}
	}

	class ClosingBrace implements Brace {
		private ClosingBrace() {
		}

		@Override
		@Nonnull
		public String value() {
			return ")";
		}

		@Override
		public String toString() {
			return "ClosingBrace";
		}
	}

	OpeningBrace OPENING_BRACE = new OpeningBrace();
	ClosingBrace CLOSING_BRACE = new ClosingBrace();
}
