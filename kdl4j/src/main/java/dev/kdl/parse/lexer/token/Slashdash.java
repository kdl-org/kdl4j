package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;

/**
 * Token for a slashdash, including all the spaces after.
 *
 * @param value the value of the token
 * @param span  the position of the token
 */
public record Slashdash(String value, Span span) implements Token {
}
