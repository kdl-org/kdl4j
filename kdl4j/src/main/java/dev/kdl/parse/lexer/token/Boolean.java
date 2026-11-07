package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;

/**
 * Token for a boolean.
 *
 * @param value the value of the token
 * @param span  the position of the token
 */
public record Boolean(boolean booleanValue, String value, Span span) implements Token {
}
