package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

public record Newline(@Nonnull String value, @Nonnull Span span) implements Token {
	public static boolean isNewline(int c) {
		return switch (c) {
			case CR, // Carriage Return
				 LF, // Line Feed
				 0x0085, // Next Line
				 0x000B, // Vertical tab
				 0x000C, // Form Feed
				 0x2028, // Line Separator
				 0x2029 // Paragraph Separator
				-> true;
			default -> false;
		};
	}

	public static final int LF = 0x000A;
	public static final int CR = 0x000D;
}
