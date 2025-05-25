package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

/**
 * Token for node spaces. Only used by the KDL 2.0 parser.
 *
 * @param value the value of the node spaces
 * @param span  the position of the token
 */
public record NodeSpace(@Nonnull String value, @Nonnull Span span) implements Token {
}
