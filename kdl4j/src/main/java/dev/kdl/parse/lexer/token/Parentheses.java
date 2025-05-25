package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

/**
 * Token for parentheses.
 */
public interface Parentheses extends Token {

	/**
	 * Token for opening parentheses.
	 *
	 * @param span the position of the token
	 */
	record OpeningParentheses(@Nonnull Span span) implements Parentheses {
		@Nonnull
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
	record ClosingParentheses(@Nonnull Span span) implements Parentheses {
		@Nonnull
		@Override
		public String value() {
			return ")";
		}
	}

}
