package dev.kdl.parse.context;

/**
 * Represents consecutive characters in the input document.
 *
 * @param start the position of the first character of the span
 * @param end   the position of the last character of the span (inclusive)
 */
public record Span(Position start, Position end) {
	/**
	 * Creates a one character width span.
	 *
	 * @param position the start and end position
	 * @return a new span
	 */
	public static Span of(Position position) {
		return new Span(position, position);
	}

	/**
	 * Creates a one character width span.
	 *
	 * @param line   the line of the span
	 * @param column the column of the span
	 * @return a new span
	 */
	public static Span of(int line, int column) {
		var position = new Position(line, column);
		return new Span(position, position);
	}

	/**
	 * Creates a multi-character span.
	 *
	 * @param startLine   the line of the first character of the span
	 * @param startColumn the column of the first character of the span
	 * @param endLine     the line of the last character of the span
	 * @param endColumn   the column of the last character of the span
	 * @return a new span
	 */
	public static Span of(int startLine, int startColumn, int endLine, int endColumn) {
		return new Span(new Position(startLine, startColumn), new Position(endLine, endColumn));
	}
}
