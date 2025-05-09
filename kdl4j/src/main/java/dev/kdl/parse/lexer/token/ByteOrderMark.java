package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

public record ByteOrderMark(@Nonnull Span span) implements Token {
	@Nonnull
	@Override
	public String value() {
		return STRING_VALUE;
	}

	public static final char VALUE = 0xFEFF;
	public static final String STRING_VALUE = String.valueOf(VALUE);
}
