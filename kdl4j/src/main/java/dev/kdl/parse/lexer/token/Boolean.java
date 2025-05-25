package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

/**
 * Token for a boolean.
 *
 * @param value the value of the token
 * @param span  the position of the token
 */
public record Boolean(boolean booleanValue, @Nonnull String value, @Nonnull Span span) implements Token {
}
