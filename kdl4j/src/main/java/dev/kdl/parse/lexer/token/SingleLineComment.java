package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;

/**
 * Token for a single-line comment.
 *
 * @param value the value of the comment
 * @param span  the position of the token
 */
public record SingleLineComment(String value, Span span) implements Token {
}
