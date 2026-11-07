package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;

/**
 * Token for parentheses.
 */
public interface Parentheses extends Token {

	/**
	 * Token for opening parentheses.
	 *
	 * @param span the position of the token
	 */
	record OpeningParentheses(Span span) implements Parentheses {
		@Override
		public String value() {
			return "(";
		}
	}

	/**
	 * Token for closing parentheses.
	 *
	 * @param span the position of the token
	 */
	record ClosingParentheses(Span span) implements Parentheses {
		@Override
		public String value() {
			return ")";
		}
	}

}
