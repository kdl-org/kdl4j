package dev.kdl.parse.lexer.token;

import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

public record NodeSpace(@Nonnull String value, @Nonnull Span span) implements Token {
	@Override
	public String toString() {
		return "NodeSpace(value='" + displaySpaces(value) + "', span=" + span + ')';
	}

	@Nonnull
	public static String displaySpaces(String value) {
		var spaces = new StringBuilder();
		value.chars().forEachOrdered(c -> {
			if (c == 0x0009 || c == 0x0020 || !isWhitespace(c)) {
				spaces.appendCodePoint(c);
			} else {
				spaces.append(String.format("\\u%04x", c));
			}
		});
		return spaces.toString();
	}

	public static boolean isWhitespace(int c) {
		return switch (c) {
			case 0x0009, // Character Tabulation
				 0x0020, // Space
				 0x00A0, // No-Break Space
				 0x1680, // Ogham Space Mark
				 0x2000, // En Quad
				 0x2001, // Em Quad
				 0x2002, // En Space
				 0x2003, // Em Space
				 0x2004, // Three-Per-Em Space
				 0x2005, // Four-Per-Em Space
				 0x2006, // Six-Per-Em Space
				 0x2007, // Figure Space
				 0x2008, // Punctuation Space
				 0x2009, // Thin Space
				 0x200A, // Hair Space
				 0x202F, // Narrow No-Break Space
				 0x205F, // Medium Mathematical Space
				 0x3000 // Ideographic Space
				-> true;
			default -> false;
		};
	}
}
