package dev.kdl.parse.lexer.reader;

import java.io.IOException;

public class KdlReadException extends IOException {
	public KdlReadException(String message) {
		super(message);
	}
}
