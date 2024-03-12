package kdl.parse.lexer.token;

import jakarta.annotation.Nonnull;
import java.util.Objects;
import kdl.parse.lexer.Token;

public class Whitespace implements Token {
	public Whitespace(char value) {
		this.value = value;
	}

	@Override
	@Nonnull
	public String value() {
		return String.valueOf(value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		var that = (Whitespace) o;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return String.format("Whitespace(\\u%04x)", (int) value);
	}

	private final char value;

	public static boolean isWhitespace(int c) {
		switch (c) {
			case 0x0009: // Character Tabulation
			case 0x000B: // Line Tabulation
			case 0x0020: // Space
			case 0x00A0: // No-Break Space
			case 0x1680: // Ogham Space Mark
			case 0x2000: // En Quad
			case 0x2001: // Em Quad
			case 0x2002: // En Space
			case 0x2003: // Em Space
			case 0x2004: // Three-Per-Em Space
			case 0x2005: // Four-Per-Em Space
			case 0x2006: // Six-Per-Em Space
			case 0x2007: // Figure Space
			case 0x2008: // Punctuation Space
			case 0x2009: // Thin Space
			case 0x200A: // Hair Space
			case 0x202F: // Narrow No-Break Space
			case 0x205F: // Medium Mathematical Space
			case 0x3000: // Ideographic Space
				return true;
			default:
				return false;
		}
	}
}
