package dev.kdl.parse.context;

public record Span(Position start, Position end) {
	public static Span of(Position position) {
		return new Span(position, position);
	}

	public static Span of(int line, int column) {
		var position = new Position(line, column);
		return new Span(position, position);
	}

	public static Span of(int startLine, int startColumn, int endLine, int endColumn) {
		return new Span(new Position(startLine, startColumn), new Position(endLine, endColumn));
	}
}
