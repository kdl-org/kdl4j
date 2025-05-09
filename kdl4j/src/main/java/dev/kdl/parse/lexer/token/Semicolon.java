package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

public record Semicolon(@Nonnull Span span) implements Token {
	@Nonnull
	@Override
	public String value() {
		return ";";
	}
}
