package dev.kdl.print;

import java.io.IOException;

class KdlPrintException extends RuntimeException {
	public KdlPrintException(IOException cause) {
		super(cause);
	}

	@Override
	public synchronized IOException getCause() {
		return (IOException) super.getCause();
	}
}
