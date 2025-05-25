package dev.kdl.print;

import dev.kdl.KdlBoolean;
import dev.kdl.KdlDocument;
import dev.kdl.KdlNode;
import dev.kdl.KdlNull;
import dev.kdl.KdlNumber;
import dev.kdl.KdlString;
import dev.kdl.KdlValue;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.IntStream;

abstract class KdlPrinterContext {
	/**
	 * Creates a new printing context.
	 *
	 * @param writer        the writer to write to
	 * @param configuration the configuration to use when printing the document
	 */
	protected KdlPrinterContext(Writer writer, KdlPrinterConfiguration configuration) {
		this.writer = writer;
		this.configuration = configuration;
	}

	/**
	 * Checks if a codepoint is an identifier character.
	 *
	 * @param c a codepoint
	 * @return true if c is an identifier character, false otherwise
	 */
	protected abstract boolean isIdentifierChar(int c);

	/**
	 * Prints a null value.
	 *
	 * @param kdlNull a null value to print
	 */
	protected abstract void printNull(KdlNull kdlNull);

	/**
	 * Prints a boolean.
	 *
	 * @param kdlBoolean a boolean to print
	 */
	protected abstract void printBoolean(KdlBoolean kdlBoolean);

	/**
	 * Prints a NaN value.
	 *
	 * @param notANumber a NaN value to print
	 */
	protected abstract void printNotANumber(KdlNumber.NotANumber notANumber);

	/**
	 * Prints a positive infinity number.
	 *
	 * @param positiveInfinity a positive infinity number to print
	 */
	protected abstract void printPositiveInfinity(KdlNumber.PositiveInfinity positiveInfinity);

	/**
	 * Prints a negative infinity number.
	 *
	 * @param negativeInfinity a negative infinity number to print
	 */
	protected abstract void printNegativeInfinity(KdlNumber.NegativeInfinity negativeInfinity);

	/**
	 * Tries to escape a codepoint for use in a string.
	 *
	 * @param c the codepoint to escape
	 * @return a string containing the escaped codepoint, or {@code null} if no escaping is required
	 */
	@Nullable
	protected abstract String escape(int c);

	/**
	 * Checks if a codepoint can be a valid first character for an identifier.
	 *
	 * @param c a codepoint
	 * @return {@code true} if the codepoint can start an identifier, {@code false} otherwise
	 */
	protected abstract boolean isValidStartOfIdentifier(int c);

	void printDocument(KdlDocument document) throws IOException {
		try {
			printNodes(document.nodes());
		} catch (KdlPrintException e) {
			throw e.getCause();
		}
	}

	private void printNode(KdlNode node) {
		printType(node.type());
		writeIdentifier(node.name());

		for (var argument : node.arguments()) {
			if (configuration.printNullArguments() || !(argument instanceof KdlNull)) {
				write(' ');
				printValue(argument);
			}
		}

		var properties = configuration.propertiesOrder().sort(node.properties().propertyNames());
		for (var property : properties) {
			var values = node.properties().getValues(property);
			if (configuration.printDuplicateProperties()) {
				values.forEach(value -> printProperty(property, value));
			} else if (!values.isEmpty()) {
				printProperty(property, values.get(values.size() - 1));
			}
		}

		if (configuration.printEmptyChildren() || !node.children().isEmpty()) {
			write(" {");
			write(configuration.newline());
			depth += 1;
			printNodes(node.children());
			depth -= 1;
			printIndentation();
			write("}");
		}
	}

	private void printValue(KdlValue<?> value) {
		if (value instanceof KdlNull) {
			printNull((KdlNull) value);
		} else if (value instanceof KdlString) {
			printString((KdlString) value);
		} else if (value instanceof KdlBoolean) {
			printBoolean((KdlBoolean) value);
		} else if (value instanceof KdlNumber<?>) {
			printNumber((KdlNumber<?>) value);
		}
	}

	private void printProperty(String name, KdlValue<?> value) {
		if (configuration.printNullProperties() || !(value instanceof KdlNull)) {
			write(' ');
			writeIdentifier(name);
			write('=');
			printValue(value);
		}
	}

	/**
	 * Prints a string.
	 *
	 * @param string the string to print
	 */
	protected void printString(KdlString string) {
		printType(string.type());
		writeIdentifier(string.value());
	}

	private void printNodes(List<KdlNode> nodes) {
		if (nodes.isEmpty() && depth == 0) {
			write(configuration.newline());
			return;
		}

		for (var node : nodes) {
			printIndentation();
			printNode(node);
			if (configuration.printSemicolons()) {
				write(';');
			}
			write(configuration.newline());
		}
	}

	private void printNumber(KdlNumber<?> number) {
		if (number instanceof KdlNumber.NotANumber) {
			printNotANumber((KdlNumber.NotANumber) number);
		} else if (number instanceof KdlNumber.PositiveInfinity) {
			printPositiveInfinity((KdlNumber.PositiveInfinity) number);
		} else if (number instanceof KdlNumber.NegativeInfinity) {
			printNegativeInfinity((KdlNumber.NegativeInfinity) number);
		} else if (number instanceof KdlNumber.Integer) {
			printInteger((KdlNumber.Integer) number);
		} else if (number instanceof KdlNumber.Decimal) {
			printDecimal((KdlNumber.Decimal) number);
		}
	}

	private void printInteger(KdlNumber.Integer integer) {
		printType(integer.type());
		write(integer.value().toString());
	}

	private void printDecimal(KdlNumber.Decimal decimal) {
		printType(decimal.type());
		write(configuration.exponentChar().replaceExponentCharacter(decimal.value().toString()));
	}

	private void printIndentation() {
		IntStream.range(0, depth).forEach(i -> write(configuration.indentation()));
	}

	/**
	 * Prints the type of a node or a value.
	 *
	 * @param type the type to print
	 */
	protected void printType(@Nullable String type) {
		if (type != null) {
			write('(');
			writeIdentifier(type);
			write(')');
		}
	}

	private void writeIdentifier(String string) {
		if (string.isEmpty()) {
			write("\"\"");
		} else {
			var builder = new StringBuilder();
			var needsQuotes = escapeString(string, builder);

			if (configuration.printQuotes() || needsQuotes) {
				write('"');
				write(builder.toString());
				write('"');
			} else {
				write(builder.toString());
			}
		}
	}

	/**
	 * Escapes character that need to be escaped in a single-line quoted string.
	 *
	 * @param string  the content of the string to escape
	 * @param builder the builder to write the escaped string to
	 * @return {@code true} if characters have been escaped, {@code false} otherwise
	 */
	protected boolean escapeString(String string, StringBuilder builder) {
		var needsQuotes = !isValidStartOfIdentifier(string.codePointAt(0));

		for (var c : string.codePoints().toArray()) {
			var escaped = escape(c);
			if (escaped != null) {
				needsQuotes = true;
				builder.append(escaped);
			} else {
				needsQuotes |= !isIdentifierChar(c);
				builder.appendCodePoint(c);
			}
		}
		return needsQuotes;
	}

	/**
	 * Writes a character
	 *
	 * @param c the character to write
	 */
	protected void write(char c) {
		try {
			writer.write(c);
		} catch (IOException e) {
			throw new KdlPrintException(e);
		}
	}

	/**
	 * Writes a string
	 *
	 * @param string the string to write
	 */
	protected void write(String string) {
		try {
			writer.write(string);
		} catch (IOException e) {
			throw new KdlPrintException(e);
		}
	}

	private int depth = 0;
	private final Writer writer;
	private final KdlPrinterConfiguration configuration;
}
