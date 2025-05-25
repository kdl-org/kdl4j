package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

/**
 * Token for a single-line comment.
 *
 * @param value the value of the comment
 * @param span  the position of the token
 */
public record SingleLineComment(@Nonnull String value, @Nonnull Span span) implements Token {
}
