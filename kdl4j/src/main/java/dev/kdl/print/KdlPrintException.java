package dev.kdl.print;

public class KdlPrintException extends RuntimeException {
	public KdlPrintException(String message) {
		super(message);
	}

	public KdlPrintException(Throwable cause) {
		super(cause);
	}
}
