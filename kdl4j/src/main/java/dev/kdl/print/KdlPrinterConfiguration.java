package dev.kdl.print;

import dev.kdl.KdlVersion;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configures a {@link KdlPrinter}.
 *
 * @param version                  the version of KDL to use for printing
 * @param indentation              the whitespace characters used for a level of indentation
 * @param newline                  the newline characters used when printing a new lineNumber
 * @param exponentChar             the character used for the exponent of decimal numbers
 * @param printEmptyChildren       whether empty children should be printed
 * @param printNullArguments       whether null arguments should be printed
 * @param printNullProperties      whether null properties should be printed
 * @param printDuplicateProperties whether duplicate properties should be printed (only the last one is printed when false)
 * @param propertiesOrder          order to use when printing properties
 * @param printSemicolons          whether semicolons should be printed after each node
 * @param printQuotes              whether quotes should be printing around identifiers
 */
public record KdlPrinterConfiguration(
	@Nonnull KdlVersion version,
	@Nonnull String indentation,
	@Nonnull String newline,
	@Nonnull ExponentCharacter exponentChar,
	boolean printEmptyChildren,
	boolean printNullArguments,
	boolean printNullProperties,
	boolean printDuplicateProperties,
	@Nonnull PropertiesOrder propertiesOrder,
	boolean printSemicolons,
	boolean printQuotes
) {
	/**
	 * @return a new builder of {@link KdlPrinterConfiguration}.a new builder of {@link KdlPrinterConfiguration}.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for {@link KdlPrinterConfiguration}.
	 */
	public static final class Builder {
		/**
		 * Sets the version of KDL.
		 *
		 * @param version the version of KDL to use
		 * @return {code this}
		 */
		@Nonnull
		public Builder version(@Nonnull KdlVersion version) {
			this.version = version;
			return this;
		}

		/**
		 * Sets the version of KDL to 1.
		 *
		 * @return {code this}
		 */
		@Nonnull
		public Builder v1() {
			this.version = KdlVersion.V1;
			return this;
		}

		/**
		 * Sets the version of KDL to 2.
		 *
		 * @return {code this}
		 */
		@Nonnull
		public Builder v2() {
			this.version = KdlVersion.V2;
			return this;
		}

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
		 * Sets the newline character to use when printing a new lineNumber. Default is "\n".
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
		 * Sets the newline characters to use when printing a new lineNumber. Default is "\n".
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
		 * Sets whether duplicate properties should be printed. If false, only the last value will be printed.
		 * Default is {@code true}.
		 *
		 * @param printDuplicateProperties whether null properties should be printed
		 * @return {@code this}
		 */
		@Nonnull
		public Builder printDuplicateProperties(boolean printDuplicateProperties) {
			this.printDuplicateProperties = printDuplicateProperties;
			return this;
		}

		/**
		 * Sets the exponent character to use when printing decimal numbers. Default is "E".
		 *
		 * @param propertiesOrder order to use when printing properties
		 * @return {@code this}
		 */
		@Nonnull
		public Builder propertiesOrder(@Nonnull PropertiesOrder propertiesOrder) {
			this.propertiesOrder = propertiesOrder;
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

		/**
		 * Sets that quotes should be printed around identifiers. Default is false.
		 *
		 * @return {@code this}
		 */
		@Nonnull
		public Builder printQuotes() {
			printQuotes = true;
			return this;
		}

		/**
		 * Sets whether quotes should be printed around identifiers. Default is false.
		 *
		 * @param printQuotes whether quotes should be printed when not required
		 * @return {@code this}
		 */
		@Nonnull
		public Builder printQuotes(boolean printQuotes) {
			this.printQuotes = printQuotes;
			return this;
		}

		/**
		 * Builds a new {@link KdlPrinterConfiguration}.
		 *
		 * @return the corresponding configuration
		 */
		@Nonnull
		public KdlPrinterConfiguration build() {
			return new KdlPrinterConfiguration(
				version,
				indentation.stream().map(Whitespace::getValue).collect(Collectors.joining()),
				newline.stream().map(Newline::getValue).collect(Collectors.joining()),
				exponentChar,
				printEmptyChildren,
				printNullArguments,
				printNullProperties,
				printDuplicateProperties,
				propertiesOrder,
				printSemicolons,
				printQuotes
			);
		}

		@Nonnull
		private KdlVersion version = KdlVersion.V2;
		@Nonnull
		private List<Whitespace> indentation = List.of(Whitespace.CHARACTER_TABULATION);
		@Nonnull
		private List<Newline> newline = List.of(Newline.LF);
		@Nonnull
		private ExponentCharacter exponentChar = ExponentCharacter.E;
		private boolean printEmptyChildren = false;
		private boolean printNullArguments = true;
		private boolean printNullProperties = true;
		private boolean printDuplicateProperties = true;
		@Nonnull
		private PropertiesOrder propertiesOrder = PropertiesOrder.DECLARATION;
		private boolean printSemicolons = false;
		private boolean printQuotes = false;
	}

	/**
	 * Valid newline characters that can be used when printing a KDL document.
	 */
	public enum Newline {
		/**
		 * Carriage return
		 */
		CR("\r"),
		/**
		 * Line feed
		 */
		LF("\n"),
		/**
		 * Carriage return + line feed
		 */
		CRLF("\r\n"),
		/**
		 * Next line
		 */
		NEXT_LINE("\u0085"),
		/**
		 * Form feed
		 */
		FORM_FEED("\u000C"),
		/**
		 * Line separator
		 */
		LINE_SEPARATOR("\u2028"),
		/**
		 * Paragraph separator
		 */
		PARAGRAPH_SEPARATOR("\u2029");

		Newline(String value) {
			this.value = value;
		}

		/**
		 * @return string value of the newline character
		 */
		public String getValue() {
			return value;
		}

		private final String value;
	}

	/**
	 * Valid characters to use for indentation.
	 */
	public enum Whitespace {
		/**
		 * A horizontal tabulation (\t).
		 */
		CHARACTER_TABULATION("\t"),
		/**
		 * A line (vertical) tabulation.
		 */
		LINE_TABULATION("\u000B"),
		/**
		 * A regular space.
		 */
		SPACE(" "),
		/**
		 * A non-breaking space.
		 */
		NO_BREAK_SPACE("\u00A0"),
		/**
		 * Ogham space mark
		 */
		OGHAM_SPACE_MARK("\u1680"),
		/**
		 * En quad
		 */
		EN_QUAD("\u2000"),
		/**
		 * Em quad
		 */
		EM_QUAD("\u2001"),
		/**
		 * En space
		 */
		EN_SPACE("\u2002"),
		/**
		 * Em space
		 */
		EM_SPACE("\u2003"),
		/**
		 * Three-per-em space
		 */
		THREE_PER_EM_SPACE("\u2004"),
		/**
		 * Four-per-em space
		 */
		FOUR_PER_EM_SPACE("\u2005"),
		/**
		 * Six-per-em space
		 */
		SIX_PER_EM_SPACE("\u2006"),
		/**
		 * Figure Space
		 */
		FIGURE_SPACE("\u2007"),
		/**
		 * Punctuation space
		 */
		PUNCTUATION_SPACE("\u2008"),
		/**
		 * Thin space
		 */
		THIN_SPACE("\u2009"),
		/**
		 * Hair space
		 */
		HAIR_SPACE("\u200A"),
		/**
		 * Narrow non-breaking space
		 */
		NARROW_NO_BREAK_SPACE("\u202F"),
		/**
		 * Medium mathematical space
		 */
		MEDIUM_MATHEMATICAL_SPACE("\u205F"),
		/**
		 * Ideographic space
		 */
		IDEOGRAPHIC_SPACE("\u3000");

		Whitespace(String value) {
			this.value = value;
		}

		/**
		 * @return the string corresponding to this whitespace
		 */
		public String getValue() {
			return value;
		}

		private final String value;
	}

	/**
	 * The characters that can be used for an exponential number.
	 */
	public enum ExponentCharacter {
		/**
		 * Lowercase e
		 */
		e,
		/**
		 * Uppercase E
		 */
		E;

		/**
		 * Replaces the exponential character in a string produced by {@link java.math.BigDecimal#toString()} if required.
		 *
		 * @param decimalAsString a string representing a number
		 * @return the same string with the exponential character replaced if required
		 */
		public String replaceExponentCharacter(String decimalAsString) {
			return this == E ? decimalAsString : decimalAsString.replace('E', 'e');
		}
	}

	/**
	 * Different orders that can be used to sort properties.
	 */
	public enum PropertiesOrder {
		/**
		 * Order properties by declaration order.
		 */
		DECLARATION,
		/**
		 * Order properties by ascending name.
		 */
		NAME_ASCENDING;

		/**
		 * Sorts a collection of property names according to this order.
		 *
		 * @param propertyNames the names to sort
		 * @return a sorted list of property names
		 */
		public List<String> sort(Collection<String> propertyNames) {
			return switch (this) {
				case DECLARATION -> propertyNames.stream().toList();
				case NAME_ASCENDING -> propertyNames.stream().sorted().toList();
			};
		}
	}
}
