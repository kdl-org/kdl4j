package kdl.parse.lexer;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import kdl.parse.KDLInternalException;
import kdl.parse.KDLParseException;
import kdl.parse.error.ErrorUtils;

import static kdl.parse.lexer.token.Newline.CR;
import static kdl.parse.lexer.token.Newline.LF;
import static kdl.parse.lexer.token.Newline.isNewline;

class KDLReader implements AutoCloseable {
	public KDLReader(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public void close() throws Exception {
		invalidated = true;
		inputStream.close();
	}

	public int peek() throws IOException {
		return peek(1);
	}

	public int peek(int n) throws IOException {
		ensureNotInvalidated("peek");

		if (n < 0 || n > peekedChars.length) {
			throw new KDLInternalException("Error while peeking: n should be between 1 and " + peekedChars.length + " included.");
		}

		while (peekedCharsNumber < n) {
			peekNextChar();
		}

		return peekedChars[n - 1];
	}

	private void peekNextChar() throws IOException {
		peekedChars[peekedCharsNumber] = readNextChar();
		peekedCharsNumber += 1;
	}

	public int read() throws IOException {
		ensureNotInvalidated("read");

		var c = nextChar();
		updatePosition(c);

		return c;
	}

	private int nextChar() throws IOException {
		int c;

		if (peekedCharsNumber > 0) {
			c = peekedChars[0];
			peekedChars = Arrays.copyOfRange(peekedChars, 1, peekedChars.length + 1);
			peekedCharsNumber -= 1;
		} else {
			c = readNextChar();
		}

		return c;
	}

	private int readNextChar() throws IOException {
		var c = inputStream.read();
		if (c < 0 || (c & 0x80) == 0) {
			return checkCodePoint(c);
		} else if ((c & 0xE0) == 0xC0) {
			var c2 = inputStream.read();
			if ((c2 & 0xC0) == 0x80) {
				return checkCodePoint(((c & 0x1F) << 6) | (c2 & 0x3F));
			}
		} else if ((c & 0xF0) == 0xE0) {
			var c2 = inputStream.read();
			if ((c2 & 0xC0) == 0x80) {
				var c3 = inputStream.read();
				if ((c3 & 0xC0) == 0x80) {
					return checkCodePoint(((c & 0xF) << 12) | ((c2 & 0x3F) << 6) | (c3 & 0x3F));
				}
			}
		} else if ((c & 0xF8) == 0xF0) {
			var c2 = inputStream.read();
			if ((c2 & 0xC0) == 0x80) {
				var c3 = inputStream.read();
				if ((c3 & 0xC0) == 0x80) {
					var c4 = inputStream.read();
					if ((c4 & 0xC0) == 0x80) {
						return checkCodePoint(((c & 0x7) << 18) | ((c2 & 0x3F) << 12) | ((c3 & 0x3F) << 6) | (c4 & 0x3F));
					}
				}
			}
		}
		throw new KDLParseException(error(String.format("Invalid character U+%x", c)));
	}

	private int checkCodePoint(int codePoint) {
		if (codePoint != EOF && isInvalid(codePoint)) {
			currentLine.appendCodePoint(codePoint);
			throw new KDLParseException(error("invalid codepoint"));
		}
		return codePoint;
	}

	private boolean isInvalid(int codePoint) {
		return codePoint <= 8
			   || (codePoint >= 0x000E && codePoint <= 0x01F)
			   || codePoint == 0x007F
			   || (codePoint >= 0xD800 && codePoint <= 0xDFFF)
			   || (codePoint >= 0x200E && codePoint <= 0x200F)
			   || (codePoint >= 0x202A && codePoint <= 0x202E)
			   || (codePoint >= 0x2066 && codePoint <= 0x2069);
	}

	private void updatePosition(int c) throws IOException {
		if (c != EOF) {
			if (isNewline(c)) {
				if (c != CR || peek() != LF) {
					currentLine.setLength(0);
					line += 1;
					column = 0;
				}
			} else {
				currentLine.appendCodePoint(c);
				column += 1;
			}
		}
	}

	public int line() {
		return line;
	}

	public int column() {
		return column;
	}

	@Nonnull
	public String error(String message) {
		return error(message, line, column);
	}

	@Nonnull
	public String error(String message, int line, int column) {
		invalidate();
		return ErrorUtils.errorMessage(currentLine, message, line, column);
	}

	private void invalidate() {
		if (!invalidated) {
			try {
				int c;
				if (peekedCharsNumber > 0) {
					c = peekedChars[0];
					peekedChars = Arrays.copyOfRange(peekedChars, 1, peekedChars.length + 1);
					peekedCharsNumber -= 1;
				} else {
					c = inputStream.read();
				}
				while (!isNewline(c) && c != EOF) {
					currentLine.appendCodePoint(c);
					c = inputStream.read();
				}
			} catch (IOException e) {
				currentLine.append("<Read Error>");
			}
		}

		invalidated = true;
	}

	private void ensureNotInvalidated(String action) {
		if (invalidated) throw new KDLInternalException("Trying to " + action + " from an invalidated reader");
	}

	private final InputStream inputStream;
	private int[] peekedChars = new int[MAX_PEEKS];
	private int peekedCharsNumber = 0;

	private final StringBuilder currentLine = new StringBuilder();
	private int line = 1;
	private int column = 0;
	private boolean invalidated = false;

	private static final int MAX_PEEKS = 2;
	private static final int EOF = -1;

}
