package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

public record Boolean(boolean booleanValue, @Nonnull String value, @Nonnull Span span) implements Token {
}
