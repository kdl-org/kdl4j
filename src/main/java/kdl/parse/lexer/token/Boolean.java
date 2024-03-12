package kdl.parse.lexer.token;

import jakarta.annotation.Nonnull;
import kdl.parse.lexer.Token;

public class Boolean implements Token {

	private Boolean(boolean value) {
		this.value = value;
	}

	@Override
	@Nonnull
	public String value() {
		return value ? "#true" : "#false";
	}

	@Override
	public String toString() {
		return value();
	}

	private final boolean value;

	public static final Boolean TRUE = new Boolean(true);
	public static final Boolean FALSE = new Boolean(false);

}
