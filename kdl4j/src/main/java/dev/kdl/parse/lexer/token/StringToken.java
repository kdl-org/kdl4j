package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;

/**
 * A token for a string value. It matches identifiers, quoted strings, raw strings, multi-line strings, and multi-line
 * raw strings.
 *
 * @param value the content of the string, with newlines appropriately escaped
 * @param span  the position of the token
 */
public record StringToken(String value, Span span) implements Token {
}
