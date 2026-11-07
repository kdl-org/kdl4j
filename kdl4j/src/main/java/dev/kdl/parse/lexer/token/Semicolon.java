package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;

/**
 * Token for a semicolon.
 *
 * @param span the span of the token
 */
public record Semicolon(Span span) implements Token {
	@Override
	public String value() {
		return ";";
	}
}
