package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;

/**
 * Token for a line continuation. Only used by the KDL 1.0 parser
 *
 * @param value the value of the line continuation, including newline characters.
 * @param span  the position of the token
 */
public record LineContinuation(String value, Span span) implements Token {
}
