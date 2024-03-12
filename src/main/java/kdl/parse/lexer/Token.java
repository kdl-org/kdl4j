package kdl.parse.lexer;

import jakarta.annotation.Nonnull;

public interface Token {
	@Nonnull
	String value();
}
