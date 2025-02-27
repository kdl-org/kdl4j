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

import static dev.kdl.parse.lexer.token.Number.isSign;
import static dev.kdl.parse.lexer.token.StringToken.IdentifierString.isIdentifierChar;
import static dev.kdl.parse.lexer.token.StringToken.IdentifierString.isUnambiguousIdentifierChar;

public class Kdl2PrinterContext extends KdlPrinterContext {
	Kdl2PrinterContext(Writer writer, KdlPrinterConfiguration configuration) {
		super(writer, configuration);
	}

	@Override
	void printDocument(KdlDocument document) {
		printNodes(document.nodes());
	}

	private void printNode(KdlNode node) {
		printType(node.type());
		writeString(node.name());

		for (var argument : node.arguments()) {
			if (configuration.shouldPrintNullArguments() || !(argument instanceof KdlNull)) {
				write(' ');
				printValue(argument);
			}
		}

		for (var property : node.properties()) {
			if (configuration.shouldPrintNullProperties() || !(property.value() instanceof KdlNull)) {
				write(' ');
				writeString(property.name());
				write('=');
				printValue(property.value());
			}
		}

		if (configuration.shouldPrintEmptyChildren() || !node.children().isEmpty()) {
			write(" {");
			write(configuration.getNewline());
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

	private void printNull(KdlNull kdlNull) {
		printType(kdlNull.type());
		write("#null");
	}

	private void printString(KdlString string) {
		printType(string.type());
		writeString(string.value());
	}

	private void printBoolean(KdlBoolean kdlBoolean) {
		printType(kdlBoolean.type());
		write(kdlBoolean.value() ? "#true" : "#false");
	}

	private void printNodes(List<KdlNode> nodes) {
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

	private void printNotANumber(KdlNumber.NotANumber notANumber) {
		printType(notANumber.type());
		write("#nan");
	}

	private void printPositiveInfinity(KdlNumber.PositiveInfinity positiveInfinity) {
		printType(positiveInfinity.type());
		write("#inf");
	}

	private void printNegativeInfinity(KdlNumber.NegativeInfinity negativeInfinity) {
		printType(negativeInfinity.type());
		write("#-inf");
	}

	private void printInteger(KdlNumber.Integer integer) {
		printType(integer.type());
		write(integer.value().toString());
	}

	private void printDecimal(KdlNumber.Decimal decimal) {
		printType(decimal.type());
		write(configuration.getExponentChar().replaceExponentCharacter(decimal.value().toString()));
	}

	private void printIndentation() {
		IntStream.range(0, depth).forEach(i -> write(configuration.getIndentation()));
	}

	private void printType(@Nullable String type) {
		if (type != null) {
			write('(');
			writeString(type);
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
			throw new KdlPrintException(e);
		}
	}

	private boolean isValidStartOfIdentifier(int c) {
		return isSign(c) || c == '.' || isUnambiguousIdentifierChar(c);
	}

	@Nullable
	private String escape(int c) {
		return switch (c) {
			case '\n' -> "\\n";
			case '\r' -> "\\r";
			case '\t' -> "\\t";
			case '\\' -> "\\\\";
			case '"' -> "\\\"";
			case '\b' -> "\\b";
			case '\f' -> "\\f";
			default -> null;
		};
	}
}
