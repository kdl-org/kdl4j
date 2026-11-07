package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;

/**
 * Token for a newline. Consecutive CR and LF characters (\r\n) are represented by one token.
 *
 * @param value the value of the newline
 * @param span  the position of the token
 */
public record Newline(String value, Span span) implements Token {
}
