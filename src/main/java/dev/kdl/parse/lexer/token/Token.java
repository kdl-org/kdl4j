package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

public interface Token {
	@Nonnull
	String value();

	@Nonnull
	Span span();
}
