package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;

/**
 * Token for the null keyword.
 *
 * @param span the position of the token
 */
public record Null(Span span) implements Token {
	@Override
	public String value() {
		return "#null";
	}
}
