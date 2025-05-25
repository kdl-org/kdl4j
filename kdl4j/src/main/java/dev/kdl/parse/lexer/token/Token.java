package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

/**
 * A token produced by a KDL lexer.
 */
public interface Token {
	/**
	 * The value of the token. The value is not always the same as the read characters.
	 *
	 * @return the value of the token
	 */
	@Nonnull
	String value();

	/**
	 * The position of the token in the input stream of file.
	 *
	 * @return a span for the token
	 */
	@Nonnull
	Span span();
}
