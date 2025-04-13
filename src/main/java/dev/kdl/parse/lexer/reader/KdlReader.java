package dev.kdl.parse.lexer.reader;

import dev.kdl.parse.KdlInternalParseException;
import jakarta.annotation.Nonnull;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class KdlReader implements Closeable, AutoCloseable {

	public KdlReader(@Nonnull InputStream inputStream, int capacity, @Nonnull InvalidCodepoint invalidCodepoint) {
		this.inputStream = inputStream;
		this.peekedChars = new IntRingBuffer(capacity);
		this.invalidCodepoint = invalidCodepoint;
	}

	public final int peek() throws IOException {
		return peek(0);
	}

	public final int peek(int n) throws IOException {
		if (n < 0 || n >= peekedChars.capacity()) {
			throw new KdlInternalParseException("Error while peeking: n should be between 0 and " + (peekedChars.capacity() - 1) + " included but was " + n);
		}

		while (peekedChars.size() <= n) {
			var c = readNextChar();
			if (c == EOF) {
				return EOF;
			}
			peekedChars.addLast(c);
		}

		return peekedChars.get(n);
	}

	public final int read() throws IOException {
		if (!peekedChars.isEmpty()) {
			return peekedChars.removeFirst();
		}
		return readNextChar();
	}

	private int readNextChar() throws IOException {
		var codepoint = readUtf8Codepoint();
		if (codepoint != EOF && invalidCodepoint.isInvalid(codepoint)) {
			throw new KdlReadException(String.format("Invalid codepoint in a KDL document: U+%04X", codepoint));
		}
		return codepoint;
	}

	private int readUtf8Codepoint() throws IOException {
		var c = inputStream.read();
		if (c == EOF) {
			return EOF;
		} else if ((c & 0b1000_0000) == 0) {
			// 1-byte codepoint
			return c;
		} else if ((c & 0b1110_0000) == 0b1100_0000) {
			// 2-bytes codepoint
			var c2 = inputStream.read();
			if (isInvalidExtraUtf8Byte(c2)) {
				throw new KdlReadException(String.format("Invalid 2-bytes UTF-8 codepoint: 0x%02X 0x%02X", c, c2));
			}
			return (c & 0b0001_1111) << 6 | (c2 & 0b0011_1111);
		} else if ((c & 0b1111_0000) == 0b1110_0000) {
			// 3-bytes codepoint
			var c2 = inputStream.read();
			var c3 = inputStream.read();
			if (isInvalidExtraUtf8Byte(c2) || isInvalidExtraUtf8Byte(c3)) {
				throw new KdlReadException(String.format("Invalid 3-bytes UTF-8 codepoint: 0x%02X 0x%02X 0x%02X", c, c2, c3));
			}
			return (c & 0b0000_1111) << 12 | (c2 & 0b0011_1111) << 6 | (c3 & 0b0011_1111);
		} else if ((c & 0b1111_1000) == 0b1111_0000) {
			// 4-bytes codepoint
			var c2 = inputStream.read();
			var c3 = inputStream.read();
			var c4 = inputStream.read();
			if (isInvalidExtraUtf8Byte(c2) || isInvalidExtraUtf8Byte(c3) || isInvalidExtraUtf8Byte(c4)) {
				throw new KdlReadException(String.format("Invalid 4-bytes UTF-8 codepoint: 0x%02X 0x%02X 0x%02X 0x%02X", c, c2, c3, c4));
			}
			return (c & 0b0000_0111) << 18 | (c2 & 0b0011_1111) << 12 | (c3 & 0b0011_1111) << 6 | (c4 & 0b0011_1111);
		}
		throw new KdlReadException(String.format("Invalid 1-byte UTF-8 codepoint: 0x%X", c));
	}

	private boolean isInvalidExtraUtf8Byte(int b) {
		return (b & 0b1100_0000) != 0b1000_0000;
	}

	@Override
	public final void close() throws IOException {
		inputStream.close();
	}

	@Nonnull
	private final InputStream inputStream;
	@Nonnull
	private final IntRingBuffer peekedChars;
	@Nonnull
	private final InvalidCodepoint invalidCodepoint;

	public static final int EOF = -1;

	@FunctionalInterface
	public interface InvalidCodepoint {
		boolean isInvalid(int codepoint);
	}
}
