package kdl.parse.lexer.token;

import jakarta.annotation.Nonnull;
import kdl.parse.lexer.Token;

public class Semicolon implements Token {
	private Semicolon() {}

	@Nonnull
	@Override
	public String value() {
		return ";";
	}

	@Override
	public String toString() {
		return "Semicolon";
	}

	public static final Semicolon INSTANCE = new Semicolon();
}
