package kdl.parse.lexer.token;

import jakarta.annotation.Nonnull;
import kdl.parse.lexer.Token;

public class Null implements Token {
	private Null() {
	}

	@Override
	@Nonnull
	public String value() {
		return "#null";
	}

	@Override
	public String toString() {
		return value();
	}

	public static final Null INSTANCE = new Null();
}
