package dev.kdl.parse.context;

import dev.kdl.parse.KdlInternalParseException;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SourceLines {
	public void append(int c) {
		line.appendCodePoint(c);
		currentLine = nextLine;
		currentColumn = nextColumn;
		nextColumn += 1;
	}

	public void newline() {
		lines.add(line.toString().stripTrailing());
		line.setLength(0);
		nextLine = nextLine + 1;
		nextColumn = 1;
	}

	public Position getCurrentPosition() {
		if (currentLine == 0 || currentColumn == 0) {
			throw new KdlInternalParseException("current position cannot be returned yet");
		}
		return new Position(currentLine, currentColumn);
	}

	public Position getNextPosition() {
		return new Position(nextLine, nextColumn);
	}

	@Nonnull
	public List<SourceLine> getLines(int startLine, int endLine) {
		return IntStream.range(Math.max(1, startLine), Math.min(lines.size(), endLine) + 1)
			.mapToObj(lineNumber -> new SourceLine(lineNumber, lines.get(lineNumber - 1)))
			.toList();
	}

	private final List<String> lines = new ArrayList<>();
	private final StringBuilder line = new StringBuilder();
	private int currentLine;
	private int currentColumn;
	private int nextLine = 1;
	private int nextColumn = 1;
}
