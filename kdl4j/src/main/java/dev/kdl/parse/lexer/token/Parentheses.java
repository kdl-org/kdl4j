package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

public interface Parentheses extends Token {

	record OpeningParentheses(@Nonnull Span span) implements Parentheses {
		@Nonnull
		@Override
		public String value() {
			return "(";
		}
	}

	record ClosingParentheses(@Nonnull Span span) implements Parentheses {
		@Nonnull
		@Override
		public String value() {
			return ")";
		}
	}

}
