package kdl.parse.lexer.token;

import jakarta.annotation.Nonnull;
import kdl.parse.lexer.Token;

public class Slashdash implements Token {
	private Slashdash() {
	}

	@Override
	@Nonnull
	public String value() {
		return VALUE;
	}

	@Override
	public String toString() {
		return "Slashdash";
	}

	private static final String VALUE = "/-";
	public static final Slashdash INSTANCE = new Slashdash();
}
