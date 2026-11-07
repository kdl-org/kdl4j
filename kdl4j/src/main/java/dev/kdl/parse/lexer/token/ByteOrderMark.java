package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;

/**
 * Token for the byte-order mark character (0xFEFF).
 *
 * @param span the position of the token
 */
public record ByteOrderMark(Span span) implements Token {
	@Override
	public String value() {
		return STRING_VALUE;
	}

	/**
	 * The byte-order mark character.
	 */
	public static final char VALUE = 0xFEFF;
	private static final String STRING_VALUE = String.valueOf(VALUE);
}
