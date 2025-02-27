package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

public interface Brace extends Token {

	record OpeningBrace(@Nonnull Span span) implements Brace {
		@Nonnull
		@Override
		public String value() {
			return "{";
		}
	}

	record ClosingBrace(@Nonnull Span span) implements Brace {
		@Nonnull
		@Override
		public String value() {
			return "}";
		}
	}

}
