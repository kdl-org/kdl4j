package kdl.parse.lexer.token;

import jakarta.annotation.Nonnull;
import java.util.Objects;
import kdl.parse.lexer.Token;

public class EqualsSign implements Token {
	public EqualsSign(char value) {
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
		var equalSign = (EqualsSign) o;
		return Objects.equals(value, equalSign.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return "EqualsSign(" + value + ')';
	}

	private final char value;

	public static boolean isEqualsSign(int c) {
		switch (c) {
			case 0x003D: // = EQUALS SIGN
			case 0xFE66: // ﹦ SMALL EQUALS SIGN
			case 0xFF1D: // ＝ FULLWIDTH EQUALS SIGN
			case 0x1F7F0: // 🟰 HEAVY EQUALS SIGN
				return true;
			default:
				return false;
		}
	}
}
