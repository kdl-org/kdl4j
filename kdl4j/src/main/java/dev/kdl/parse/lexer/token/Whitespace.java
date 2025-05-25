package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

/**
 * Token for whitespaces.
 *
 * @param value the value of the token
 * @param span  the position of the token
 */
public record Whitespace(@Nonnull String value, @Nonnull Span span) implements Token {
}
