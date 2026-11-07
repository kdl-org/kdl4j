package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;

/**
 * Token for the equals sign.
 *
 * @param span the position of the token
 */
public record EqualsSign(Span span) implements Token {
	@Override
	public String value() {
		return "=";
	}
}
