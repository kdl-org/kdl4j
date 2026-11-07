package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;

/**
 * Token for braces.
 */
public interface Brace extends Token {

	/**
	 * Token for an opening brace.
	 *
	 * @param span the position of the token
	 */
	record OpeningBrace(Span span) implements Brace {
		@Override
		public String value() {
			return "{";
		}
	}

	/**
	 * Token for a closing brace.
	 *
	 * @param span the position of the token
	 */
	record ClosingBrace(Span span) implements Brace {
		@Override
		public String value() {
			return "}";
		}
	}

}
