package kdl.parse.lexer.token;

import jakarta.annotation.Nonnull;
import java.util.Objects;
import kdl.parse.lexer.Token;

public class Newline implements Token {
	public Newline(String value) {
		this.value = value;
	}

	public Newline(char value) {
		this.value = String.valueOf(value);
	}

	@Nonnull
	@Override
	public String value() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		var newline = (Newline) o;
		return Objects.equals(value, newline.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		switch (value) {
			case "\r":
				return "Newline(CR)";
			case "\n":
				return "Newline(LF)";
			case "\r\n":
				return "Newline(CRLF)";
			default:
				return String.format("Newline(\\u%04x)", value.codePointAt(0));
		}
	}

	private final String value;

	public static boolean isNewline(int c) {
		switch (c) {
			case CR: // Carriage Return
			case LF: // Line Feed
			case 0x0085: // Next Line
			case 0x000C: // Form Feed
			case 0x2028: // Line Separator
			case 0x2029: // Paragraph Separator
				return true;
			default:
				return false;
		}
	}

	public static final int LF = 0x000A;
	public static final int CR = 0x000D;
}
