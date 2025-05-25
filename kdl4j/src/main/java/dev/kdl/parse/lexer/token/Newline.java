package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

/**
 * Token for a newline. Consecutive CR and LF characters (\r\n) are represented by one token.
 *
 * @param value the value of the newline
 * @param span  the position of the token
 */
public record Newline(@Nonnull String value, @Nonnull Span span) implements Token {
}
