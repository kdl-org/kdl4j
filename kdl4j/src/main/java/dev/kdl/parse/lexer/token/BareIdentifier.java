package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

/**
 * Token for a bare identifier (without quotes). It is only used by the KDL 1.0 lexer.
 *
 * @param value the value of the identifier
 * @param span  the position of the token
 */
public record BareIdentifier(@Nonnull String value, @Nonnull Span span) implements Token {
}
