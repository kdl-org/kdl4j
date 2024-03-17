package kdl.parse.lexer.token;

import jakarta.annotation.Nonnull;
import java.util.Objects;
import kdl.parse.lexer.Token;

public class SingleLineComment implements Token {
	public SingleLineComment(String value) {
		this.value = value;
	}

	@Override
	@Nonnull
	public String value() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		var that = (SingleLineComment) o;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return "SingleLineComment(" + value + ')';
	}

	private final String value;
}
