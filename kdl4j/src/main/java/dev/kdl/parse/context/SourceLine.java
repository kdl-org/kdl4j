package dev.kdl.parse.context;


/**
 * Represents a line in the input.
 *
 * @param lineNumber the number of the line
 * @param line       the content of the line
 */
public record SourceLine(int lineNumber, String line) {
}
