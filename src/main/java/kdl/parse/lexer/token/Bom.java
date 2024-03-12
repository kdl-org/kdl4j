package kdl.parse.lexer.token;

import jakarta.annotation.Nonnull;
import kdl.parse.lexer.Token;

public class Bom implements Token {

	private Bom() {
	}

	@Override
	@Nonnull
	public String value() {
		return String.valueOf(VALUE);
	}

	@Override
	public String toString() {
		return "BOM";
	}

	public static final char VALUE = 0xFEFF;
	public static final Bom INSTANCE = new Bom();
}
