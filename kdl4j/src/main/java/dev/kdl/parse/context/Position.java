package dev.kdl.parse.context;

import jakarta.annotation.Nonnull;

/**
 * Represents a position in a file or stream.
 *
 * @param line   the line for this position, 1-based
 * @param column the column for this position, 1-based
 */
public record Position(int line, int column) {
	/**
	 * Creates a new position by adding an offset to the column. The line is unchanged.
	 *
	 * @param columnOffset the offset to add to the column
	 * @return a new position with the new column
	 */
	@Nonnull
	public Position withColumnOffset(int columnOffset) {
		return new Position(line, column + columnOffset);
	}
}
