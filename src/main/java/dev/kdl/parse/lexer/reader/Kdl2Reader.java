package dev.kdl.parse.lexer.reader;

import jakarta.annotation.Nonnull;

import java.io.InputStream;

public class Kdl2Reader extends KdlReader {
	public Kdl2Reader(@Nonnull InputStream inputStream, int capacity) {
		super(inputStream, capacity);
	}

	@Override
	protected boolean isInvalid(int codepoint) {
		return codepoint <= 8
			|| (codepoint >= 0x000E && codepoint <= 0x01F)
			|| codepoint == 0x007F
			|| (codepoint >= 0xD800 && codepoint <= 0xDFFF)
			|| (codepoint >= 0x200E && codepoint <= 0x200F)
			|| (codepoint >= 0x202A && codepoint <= 0x202E)
			|| (codepoint >= 0x2066 && codepoint <= 0x2069);
	}
}
