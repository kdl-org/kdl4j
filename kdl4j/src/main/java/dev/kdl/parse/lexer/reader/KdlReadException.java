package dev.kdl.parse.lexer.reader;

import java.io.IOException;

/**
 * This exception is thrown when the reader fails to read a valid Unicode codepoint.
 */
public class KdlReadException extends IOException {

	/**
	 * Creates a new {@link KdlReadException}.
	 *
	 * @param message the error message
	 */
	public KdlReadException(String message) {
		super(message);
	}
}
