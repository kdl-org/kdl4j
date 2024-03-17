package kdl.parse.error;

public class ErrorUtils {
	private ErrorUtils() {
	}

	public static String errorMessage(CharSequence currentLine, CharSequence message, int line, int column) {
		return "Error line " + line + " - " + message + ":\n" +
			   currentLine + "\n" +
			   " ".repeat(Math.max(0, column - 1)) + "▲\n" +
			   "─".repeat(Math.max(0, column - 1)) + '╯';
	}
}
