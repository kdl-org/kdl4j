package dev.kdl.parse.context;

import jakarta.annotation.Nonnull;

public record Position(int line, int column) {
	@Nonnull
	public Position withColumnOffset(int columnOffset) {
		return new Position(line, column + columnOffset);
	}
}
