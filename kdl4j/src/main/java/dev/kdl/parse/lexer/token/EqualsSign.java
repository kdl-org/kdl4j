package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

/**
 * Token for the equals sign.
 *
 * @param span the position of the token
 */
public record EqualsSign(@Nonnull Span span) implements Token {
	@Nonnull
	@Override
	public String value() {
		return "=";
	}
}
