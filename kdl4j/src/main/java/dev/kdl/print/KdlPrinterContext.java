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

public abstract class KdlPrinterContext {
	protected KdlPrinterContext(Writer writer, KdlPrinterConfiguration configuration) {
		this.writer = writer;
		this.configuration = configuration;
	}

	protected abstract boolean isIdentifierChar(int c);

	protected abstract void printNull(KdlNull kdlNull);

	protected abstract void printBoolean(KdlBoolean kdlBoolean);

	protected abstract void printNotANumber(KdlNumber.NotANumber notANumber);

	protected abstract void printPositiveInfinity(KdlNumber.PositiveInfinity positiveInfinity);

	protected abstract void printNegativeInfinity(KdlNumber.NegativeInfinity negativeInfinity);

	@Nullable
	protected abstract String escape(int c);

	protected abstract boolean isValidStartOfIdentifier(int c);

	void printDocument(KdlDocument document) {
		printNodes(document.nodes());
	}

	protected void printNode(KdlNode node) {
		printType(node.type());
		writeIdentifier(node.name());

		for (var argument : node.arguments()) {
			if (configuration.printNullArguments() || !(argument instanceof KdlNull)) {
				write(' ');
				printValue(argument);
			}
		}

		for (var property : node.properties()) {
			if (configuration.printNullProperties() || !(property.value() instanceof KdlNull)) {
				write(' ');
				writeIdentifier(property.name());
				write('=');
				printValue(property.value());
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

	protected void printValue(KdlValue<?> value) {
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

	protected void printString(KdlString string) {
		printType(string.type());
		writeIdentifier(string.value());
	}

	protected void printNodes(List<KdlNode> nodes) {
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

	protected void printNumber(KdlNumber<?> number) {
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

	protected void printInteger(KdlNumber.Integer integer) {
		printType(integer.type());
		write(integer.value().toString());
	}

	protected void printDecimal(KdlNumber.Decimal decimal) {
		printType(decimal.type());
		write(configuration.exponentChar().replaceExponentCharacter(decimal.value().toString()));
	}

	protected void printIndentation() {
		IntStream.range(0, depth).forEach(i -> write(configuration.indentation()));
	}

	protected void printType(@Nullable String type) {
		if (type != null) {
			write('(');
			writeIdentifier(type);
			write(')');
		}
	}

	protected void writeIdentifier(String string) {
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

	protected void writeString(String string) {
		if (string.isEmpty()) {
			write("\"\"");
		} else {
			var builder = new StringBuilder();

			escapeString(string, builder);

			write('"');
			write(builder.toString());
			write('"');
		}
	}

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

	protected void write(char c) {
		try {
			writer.write(c);
		} catch (IOException e) {
			throw new KdlPrintException(e);
		}
	}

	protected void write(String string) {
		try {
			writer.write(string);
		} catch (IOException e) {
			throw new KdlPrintException(e);
		}
	}

	protected int depth = 0;
	protected final Writer writer;
	protected final KdlPrinterConfiguration configuration;
}
