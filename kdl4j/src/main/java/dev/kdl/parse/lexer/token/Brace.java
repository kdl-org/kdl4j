package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

/**
 * Token for braces.
 */
public interface Brace extends Token {

	/**
	 * Token for an opening brace.
	 *
	 * @param span the position of the token
	 */
	record OpeningBrace(@Nonnull Span span) implements Brace {
		@Nonnull
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
	record ClosingBrace(@Nonnull Span span) implements Brace {
		@Nonnull
		@Override
		public String value() {
			return "}";
		}
	}

}
