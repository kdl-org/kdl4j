package dev.kdl.parse;

import dev.kdl.parse.context.ParseContext;
import dev.kdl.parse.context.SourceLine;
import dev.kdl.parse.context.Span;

import java.util.List;

public class Reporter {

	public static String getReport(KdlParseException parseException) {
		return getReport(parseException, false);
	}

	public static String getReportWithAnsiCodes(KdlParseException parseException) {
		return getReport(parseException, true);
	}

	public static String getReport(KdlParseException parseException, boolean ansiColors) {
		if (parseException instanceof KdlHybridParseException) {
			return "Failed to parse the document using both the KDL v2 and the KDL v1 parser.\n" +
				"KDL v2 error:\n" + new Reporter(((KdlHybridParseException) parseException).getV1Exception(), ansiColors).getReport() + "\n" +
				"KDL v1 error:\n" + new Reporter(((KdlHybridParseException) parseException).getV2Exception(), ansiColors).getReport();
		}
		return new Reporter(parseException, ansiColors).getReport();
	}

	private Reporter(KdlParseException parseException, boolean ansiColors) {
		message = parseException.getErrorMessage();
		context = parseException.getContext();
		label = parseException.getLabel();
		help = parseException.getHelp();
		this.ansiColors = ansiColors;
		if (context != null) {
			List<SourceLine> sourceLines = context.sourceLines();
			var lastLine = sourceLines.get(sourceLines.size() - 1);
			lineNumberSize = String.valueOf(lastLine.lineNumber()).length();
		} else {
			lineNumberSize = 0;
		}
	}

	private String getReport() {
		if (!builder.isEmpty()) {
			return builder.toString();
		}

		printErrorSign();
		print(message);

		if (context != null) {
			printContext();
		}

		if (help != null) {
			printHelpMessage();
		}

		return builder.toString();
	}

	private void printContext() {
		print(":\n");

		printMargin("╭─[");
		printWithColor(context.filename(), ANSI_CYAN_UNDERLINE);
		print(":");
		print(String.valueOf(context.span().start().line()));
		print(":");
		print(String.valueOf(context.span().start().column()));
		print("]\n");

		for (var sourceLine : context.sourceLines()) {
			printMargin(sourceLine.lineNumber());
			print(sourceLine.line());
			if (label != null) {
				printUnderline(sourceLine, context.span());
			}
			println();
		}

		printMargin("╰─");
	}

	private void printUnderline(SourceLine line, Span span) {
		if (span.start().line() <= line.lineNumber() && span.end().line() > line.lineNumber()) {
			var start = span.start().line() == line.lineNumber() ? span.start().column() : 1;
			var end = line.line().length();
			println();
			printMargin("· ");
			print(" ".repeat(start - 1));
			printArrow("─".repeat(end - start + 1));
		} else if (span.end().line() == line.lineNumber()) {
			var start = span.start().line() == line.lineNumber() ? span.start().column() : 1;
			var end = span.end().column();
			var verticalBarColumn = start + (end - start) / 2;
			println();
			printMargin("· ");
			print(" ".repeat(start - 1));
			printArrow("─".repeat(verticalBarColumn - start) + "┬" + "─".repeat(end - verticalBarColumn));

			if (label.length() < verticalBarColumn - 2) {
				println();
				printMargin("· ");
				print(" ".repeat(verticalBarColumn - 2 - label.length()));
				printArrow(label + " ╯");
			} else {
				println();
				printMargin("· ");
				print(" ".repeat(verticalBarColumn - 1));
				printArrow("╰ " + label);
			}
		}
	}

	private void printHelpMessage() {
		println();
		printHelp();
		print(help);
	}

	private void printMargin(int line) {
		print(String.format("%" + lineNumberSize + "d", line));
		print(" │ ");
	}

	private void printMargin(String separator) {
		print(" ".repeat(lineNumberSize + 1));
		print(separator);
	}

	private void printErrorSign() {
		printWithColor("× ", ANSI_ERROR);
	}

	private void printArrow(String content) {
		printWithColor(content, ANSI_ARROW);
	}

	private void printHelp() {
		if (ansiColors) {
			print(ANSI_BLUE_UNDERLINE);
		}
		print("help");
		if (ansiColors) {
			print(ANSI_REMOVE_UNDERLINE);
		}
		print(": ");
		if (ansiColors) {
			print(ANSI_RESET);
		}
	}

	private void printWithColor(String content, String color) {
		if (ansiColors) {
			print(color);
		}
		print(content);
		if (ansiColors) {
			print(ANSI_RESET);
		}
	}

	private void print(String content) {
		builder.append(content);
	}

	private void println() {
		builder.append('\n');
	}

	private final StringBuilder builder = new StringBuilder();
	private final String message;
	private final ParseContext context;
	private final String label;
	private final String help;
	private final int lineNumberSize;
	private final boolean ansiColors;

	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_ERROR = "\u001B[1;91m";
	private static final String ANSI_ARROW = "\u001B[35m";
	private static final String ANSI_BLUE_UNDERLINE = "\u001B[1;4;94m";
	private static final String ANSI_CYAN_UNDERLINE = "\u001B[1;4;96m";
	private static final String ANSI_REMOVE_UNDERLINE = "\u001B[24m";
}
