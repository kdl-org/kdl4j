package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

/**
 * Token for a slashdash, including all the spaces after.
 *
 * @param value the value of the token
 * @param span  the position of the token
 */
public record Slashdash(@Nonnull String value, @Nonnull Span span) implements Token {
}
