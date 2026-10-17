package dev.kdl.parse.context;

import dev.kdl.parse.KdlInternalParseException;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for storing the lines of the input. Mainly used for error reporting.
 */
public class SourceLines {

	/**
	 * Appends a codepoint to the current line and computes its position.
	 *
	 * @param c the codepoint to append
	 */
	public void append(int c) {
		line.appendCodePoint(c);
		currentLine = nextLine;
		currentColumn = nextColumn;
		nextColumn += 1;
	}

	/**
	 * Indicates a newline character has been reached. It does not append any character to the lines.
	 */
	public void newline() {
		lines.add(new SourceLine(nextLine, line.toString().stripTrailing()));
		line.setLength(0);
		nextLine = nextLine + 1;
		nextColumn = 1;
	}

	/**
	 * Gets the position of the last appended character.
	 *
	 * @return the position correspond to the last appended character
	 */
	public Position getCurrentPosition() {
		if (currentLine == 0 || currentColumn == 0) {
			throw new KdlInternalParseException("current position cannot be returned yet");
		}
		return new Position(currentLine, currentColumn);
	}

	/**
	 * Gets the position of the next character to be appended.
	 *
	 * @return the position correspond to the next character to be appended
	 */
	public Position getNextPosition() {
		return new Position(nextLine, nextColumn);
	}

	/**
	 * Get a list of {@link SourceLine}. If some lines are missing, only lines that are present and requested are
	 * returned. The current line is also returned, even if it has not been terminated by a newline yet.
	 *
	 * @param startLine the first line to retrieve
	 * @param endLine   the last line to retrieve (included)
	 * @return a list of lines according to the requested lines
	 */
	@Nonnull
	public List<SourceLine> getLines(int startLine, int endLine) {
		var firstLine = Math.max(1, startLine);
		var maxLine = line.length() > 0 ? lines.size() + 1 : lines.size();
		var lastLine = Math.min(maxLine, endLine);

		var result = new ArrayList<SourceLine>(Math.max(0, lastLine - firstLine + 1));
		for (var lineNumber = firstLine; lineNumber <= lastLine; lineNumber++) {
			if (lineNumber <= lines.size()) {
				result.add(lines.get(lineNumber - 1));
			} else {
				result.add(new SourceLine(lineNumber, line.toString().stripTrailing()));
			}
		}
		return result;
	}

	private final List<SourceLine> lines = new ArrayList<>();
	private final StringBuilder line = new StringBuilder();
	private int currentLine;
	private int currentColumn;
	private int nextLine = 1;
	private int nextColumn = 1;
}
