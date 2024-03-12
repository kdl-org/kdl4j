package kdl.print;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configures a {@link KDLPrinter}.
 */
public class PrinterConfiguration {
	private PrinterConfiguration(
		@Nonnull String indentation,
		@Nonnull String newline,
		@Nonnull ExponentCharacter exponentChar,
		boolean printEmptyChildren,
		boolean printNullArguments,
		boolean printNullProperties,
		boolean printSemicolons
	) {
		this.indentation = indentation;
		this.newline = newline;
		this.exponentChar = exponentChar;
		this.printEmptyChildren = printEmptyChildren;
		this.printNullArguments = printNullArguments;
		this.printNullProperties = printNullProperties;
		this.printSemicolons = printSemicolons;
	}

	/**
	 * @return the whitespace characters used for a level of indentation
	 */
	@Nonnull
	public String getIndentation() {
		return indentation;
	}

	/**
	 * @return the newline characters used when printing a new line
	 */
	@Nonnull
	public String getNewline() {
		return newline;
	}

	/**
	 * @return the character used for the exponent of decimal numbers
	 */
	@Nonnull
	public ExponentCharacter getExponentChar() {
		return exponentChar;
	}

	/**
	 * @return whether empty children should be printed
	 */
	public boolean shouldPrintEmptyChildren() {
		return printEmptyChildren;
	}

	/**
	 * @return whether null arguments should be printed
	 */
	public boolean shouldPrintNullArguments() {
		return printNullArguments;
	}

	/**
	 * @return whether null properties should be printed
	 */
	public boolean shouldPrintNullProperties() {
		return printNullProperties;
	}

	/**
	 * @return whether semicolons should be printed after each node
	 */
	public boolean shouldPrintSemicolons() {
		return printSemicolons;
	}

	@Nonnull
	private final String indentation;
	@Nonnull
	private final String newline;
	@Nonnull
	private final ExponentCharacter exponentChar;
	private final boolean printEmptyChildren;
	private final boolean printNullArguments;
	private final boolean printNullProperties;
	private final boolean printSemicolons;

	/**
	 * @return a new builder of {@link PrinterConfiguration}.a new builder of {@link PrinterConfiguration}.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for {@link PrinterConfiguration}.
	 */
	public static final class Builder {
		/**
		 * Sets the indention to a whitespace character. Default is "\t".
		 *
		 * @param indentation the whitespace character to use for indentation
		 * @return {@code this}
		 */
		@Nonnull
		public Builder indentation(@Nonnull Whitespace indentation) {
			this.indentation = List.of(indentation);
			return this;
		}

		/**
		 * Sets the indentation to whitespace characters. Default is "\t".
		 *
		 * @param indentation the whitespace characters to use for indentation
		 * @return {@code this}
		 */
		@Nonnull
		public Builder indentation(@Nonnull List<Whitespace> indentation) {
			this.indentation = indentation;
			return this;
		}

		/**
		 * Sets the newline character to use when printing a new line. Default is "\n".
		 *
		 * @param newline the newline character to use
		 * @return {@code this}
		 */
		@Nonnull
		public Builder newline(@Nonnull Newline newline) {
			this.newline = List.of(newline);
			return this;
		}

		/**
		 * Sets the newline characters to use when printing a new line. Default is "\n".
		 *
		 * @param newline the newline characters to use
		 * @return {@code this}
		 */
		@Nonnull
		public Builder newline(@Nonnull List<Newline> newline) {
			this.newline = newline;
			return this;
		}

		/**
		 * Sets the exponent character to use when printing decimal numbers. Default is "E".
		 *
		 * @param exponentChar the exponent character to use
		 * @return {@code this}
		 */
		@Nonnull
		public Builder exponentChar(@Nonnull ExponentCharacter exponentChar) {
			this.exponentChar = exponentChar;
			return this;
		}

		/**
		 * Set that empty children should be printed. Default is {@code false}.
		 *
		 * @return {@code this}
		 */
		@Nonnull
		public Builder printEmptyChildren() {
			printEmptyChildren = true;
			return this;
		}

		/**
		 * Sets whether empty children should be printed. Default is {@code false}.
		 *
		 * @param printEmptyChildren whether empty children should be printed
		 * @return {@code this}
		 */
		@Nonnull
		public Builder printEmptyChildren(boolean printEmptyChildren) {
			this.printEmptyChildren = printEmptyChildren;
			return this;
		}

		/**
		 * Sets whether null arguments should be printed. Default is {@code true}.
		 *
		 * @param printNullArguments whether null arguments should be printed
		 * @return {@code this}
		 */
		@Nonnull
		public Builder printNullArguments(boolean printNullArguments) {
			this.printNullArguments = printNullArguments;
			return this;
		}

		/**
		 * Sets whether null properties should be printed. Default is {@code true}.
		 *
		 * @param printNullProperties whether null properties should be printed
		 * @return {@code this}
		 */
		@Nonnull
		public Builder printNullProperties(boolean printNullProperties) {
			this.printNullProperties = printNullProperties;
			return this;
		}

		/**
		 * Sets that semicolons should be printed after each node. Default is false.
		 *
		 * @return {@code this}
		 */
		@Nonnull
		public Builder printSemicolons() {
			printSemicolons = true;
			return this;
		}


		/**
		 * Sets whether semicolons should be printed after each node. Default is false.
		 *
		 * @param printSemiColons whether semicolons should be printed after each node
		 * @return {@code this}
		 */
		@Nonnull
		public Builder printSemicolons(boolean printSemiColons) {
			this.printSemicolons = printSemiColons;
			return this;
		}

		@Nonnull
		public PrinterConfiguration build() {
			return new PrinterConfiguration(
				indentation.stream().map(Whitespace::getValue).collect(Collectors.joining()),
				newline.stream().map(Newline::getValue).collect(Collectors.joining()),
				exponentChar,
				printEmptyChildren,
				printNullArguments,
				printNullProperties,
				printSemicolons
			);
		}

		@Nonnull
		private List<Whitespace> indentation = List.of(Whitespace.CHARACTER_TABULATION);
		@Nonnull
		private List<Newline> newline = List.of(Newline.LF);
		@Nonnull
		private ExponentCharacter exponentChar = ExponentCharacter.E;
		private boolean printEmptyChildren = false;
		private boolean printNullArguments = true;
		private boolean printNullProperties = true;
		private boolean printSemicolons = false;
	}

	public enum Newline {
		CR("\r"),
		LF("\n"),
		CRLF("\r\n"),
		NEXT_LINE("\u0085"),
		FORM_FEED("\u000C"),
		LINE_SEPARATOR("\u2028"),
		PARAGRAPH_SEPARATOR("\u2029");

		Newline(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		private final String value;
	}

	public enum Whitespace {
		CHARACTER_TABULATION("\t"),
		LINE_TABULATION("\u000B"),
		SPACE(" "),
		NO_BREAK_SPACE("\u00A0"),
		OGHAM_SPACE_MARK("\u1680"),
		EN_QUAD("\u2000"),
		EM_QUAD("\u2001"),
		EN_SPACE("\u2002"),
		EM_SPACE("\u2003"),
		THREE_PER_EM_SPACE("\u2004"),
		FOUR_PER_EM_SPACE("\u2005"),
		SIX_PER_EM_SPACE("\u2006"),
		FIGURE_SPACE("\u2007"),
		PUNCTUATION_SPACE("\u2008"),
		THIN_SPACE("\u2009"),
		HAIR_SPACE("\u200A"),
		NARROW_NO_BREAK_SPACE("\u202F"),
		MEDIUM_MATHEMATICAL_SPACE("\u205F"),
		IDEOGRAPHIC_SPACE("\u3000");

		Whitespace(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		private final String value;
	}

	public enum ExponentCharacter {
		e, E;

		public String replaceExponentCharacter(String decimalAsString) {
			return this == E
				? decimalAsString
				: decimalAsString.replace('E', 'e');
		}
	}
}
