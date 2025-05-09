package dev.kdl.parse;

/**
 * Thrown if an unexpected state is encountered while parsing a document. If you encounter this
 * please report an issue with the offending document.
 */
public class KdlInternalParseException extends RuntimeException {
	public KdlInternalParseException(String message) {
		super(message);
	}
}
