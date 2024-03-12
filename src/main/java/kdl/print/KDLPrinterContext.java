package kdl.print;

import jakarta.annotation.Nullable;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import kdl.KDLBoolean;
import kdl.KDLDocument;
import kdl.KDLNode;
import kdl.KDLNull;
import kdl.KDLNumber;
import kdl.KDLString;
import kdl.KDLValue;

import static kdl.parse.lexer.token.Number.isSign;
import static kdl.parse.lexer.token.StringToken.IdentifierString.isIdentifierChar;
import static kdl.parse.lexer.token.StringToken.IdentifierString.isUnambiguousIdentifierChar;

public class KDLPrinterContext {
	KDLPrinterContext(Writer writer, PrinterConfiguration configuration) {
		this.writer = writer;
		this.configuration = configuration;
	}

	void printDocument(KDLDocument document) {
		var nodes = document.getNodes();
		printNodes(nodes);
	}

	private void printNode(KDLNode node) {
		printType(node.getType());
		writeString(node.getName());

		for (var argument : node.getArguments()) {
			if (configuration.shouldPrintNullArguments() || !(argument instanceof KDLNull)) {
				write(' ');
				printValue(argument);
			}
		}

		for (var property : node.getProperties()) {
			if (configuration.shouldPrintNullProperties() || !(property.getValue() instanceof KDLNull)) {
				write(' ');
				writeString(property.getName());
				write('=');
				printValue(property.getValue());
			}
		}

		if (configuration.shouldPrintEmptyChildren() || !node.getChildren().isEmpty()) {
			write(" {");
			write(configuration.getNewline());
			depth += 1;
			printNodes(node.getChildren());
			depth -= 1;
			printIndentation();
			write("}");
		}
	}

	private void printValue(KDLValue<?> value) {
		if (value instanceof KDLNull) {
			printNull((KDLNull) value);
		} else if (value instanceof KDLString) {
			printString((KDLString) value);
		} else if (value instanceof KDLBoolean) {
			printBoolean((KDLBoolean) value);
		} else if (value instanceof KDLNumber<?>) {
			printNumber((KDLNumber<?>) value);
		}
	}

	private void printNull(KDLNull kdlNull) {
		printType(kdlNull.getType());
		write("#null");
	}

	private void printString(KDLString string) {
		printType(string.getType());
		writeString(string.getValue());
	}

	private void printBoolean(KDLBoolean kdlBoolean) {
		printType(kdlBoolean.getType());
		write(kdlBoolean.getValue() ? "#true" : "#false");
	}

	private void printNodes(List<KDLNode> nodes) {
		if (nodes.isEmpty() && depth == 0) {
			write(configuration.getNewline());
			return;
		}

		for (var node : nodes) {
			printIndentation();
			printNode(node);
			if (configuration.shouldPrintSemicolons()) {
				write(';');
			}
			write(configuration.getNewline());
		}
	}

	private void printNumber(KDLNumber<?> number) {
		if (number instanceof KDLNumber.NotANumber) {
			printNotANumber((KDLNumber.NotANumber) number);
		} else if (number instanceof KDLNumber.PositiveInfinity) {
			printPositiveInfinity((KDLNumber.PositiveInfinity) number);
		} else if (number instanceof KDLNumber.NegativeInfinity) {
			printNegativeInfinity((KDLNumber.NegativeInfinity) number);
		} else if (number instanceof KDLNumber.Integer) {
			printInteger((KDLNumber.Integer) number);
		} else if (number instanceof KDLNumber.Decimal) {
			printDecimal((KDLNumber.Decimal) number);
		}
	}

	private void printNotANumber(KDLNumber.NotANumber notANumber) {
		printType(notANumber.getType());
		write("#nan");
	}

	private void printPositiveInfinity(KDLNumber.PositiveInfinity positiveInfinity) {
		printType(positiveInfinity.getType());
		write("#inf");
	}

	private void printNegativeInfinity(KDLNumber.NegativeInfinity negativeInfinity) {
		printType(negativeInfinity.getType());
		write("#-inf");
	}

	private void printInteger(KDLNumber.Integer integer) {
		printType(integer.getType());
		write(integer.getValue().toString());
	}

	private void printDecimal(KDLNumber.Decimal decimal) {
		printType(decimal.getType());
		write(configuration.getExponentChar().replaceExponentCharacter(decimal.getValue().toString()));
	}

	private void printIndentation() {
		IntStream.range(0, depth).forEach(i -> write(configuration.getIndentation()));
	}

	private void printType(Optional<String> type) {
		if (type.isPresent()) {
			write('(');
			writeString(type.get());
			write(')');
		}
	}

	private void writeString(String string) {
		try {
			if (string.isEmpty()) {
				writer.write("\"\"");
			} else {
				var needsQuotes = !isValidStartOfIdentifier(string.codePointAt(0));
				var builder = new StringBuilder();

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

				if (needsQuotes) {
					write('"');
					write(builder.toString());
					write('"');
				} else {
					write(builder.toString());
				}
			}
		} catch (IOException e) {
			throw new KDLPrintException(e);
		}
	}

	private boolean isValidStartOfIdentifier(int c) {
		return isSign(c) || c == '.' || isUnambiguousIdentifierChar(c);
	}

	@Nullable
	private String escape(int c) {
		switch (c) {
			case '\n':
				return "\\n";
			case '\r':
				return "\\r";
			case '\t':
				return "\\t";
			case '\\':
				return "\\\\";
			case '"':
				return "\\\"";
			case '\b':
				return "\\b";
			case '\f':
				return "\\f";
			default:
				return null;
		}
	}

	private void write(char c) {
		try {
			writer.write(c);
		} catch (IOException e) {
			throw new KDLPrintException(e);
		}
	}

	private void write(String string) {
		try {
			writer.write(string);
		} catch (IOException e) {
			throw new KDLPrintException(e);
		}
	}

	private int depth = 0;
	private final Writer writer;
	private final PrinterConfiguration configuration;
}
