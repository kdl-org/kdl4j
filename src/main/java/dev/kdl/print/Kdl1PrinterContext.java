package dev.kdl.print;

import dev.kdl.KdlBoolean;
import dev.kdl.KdlNull;
import dev.kdl.KdlNumber;
import dev.kdl.KdlString;
import dev.kdl.parse.lexer.helper.Kdl1CharHelper;
import jakarta.annotation.Nullable;

import java.io.Writer;

import static dev.kdl.parse.lexer.helper.KdlCharHelper.isDecimalDigit;

public class Kdl1PrinterContext extends KdlPrinterContext {
	Kdl1PrinterContext(Writer writer, KdlPrinterConfiguration configuration) {
		super(writer, configuration);
	}

	@Override
	protected boolean isIdentifierChar(int c) {
		return Kdl1CharHelper.isIdentifierChar(c);
	}

	@Override
	protected void printString(KdlString string) {
		printType(string.type());
		writeString(string.value());
	}

	@Override
	protected void printNull(KdlNull kdlNull) {
		printType(kdlNull.type());
		write("null");
	}

	@Override
	protected void printBoolean(KdlBoolean kdlBoolean) {
		printType(kdlBoolean.type());
		write(kdlBoolean.value() ? "true" : "false");
	}

	@Override
	protected void printNotANumber(KdlNumber.NotANumber notANumber) {
		throw new UnsupportedOperationException("KDL v1 does not support Not A Number");
	}

	@Override
	protected void printPositiveInfinity(KdlNumber.PositiveInfinity positiveInfinity) {
		throw new UnsupportedOperationException("KDL v1 does not support Positive Infinity");
	}

	@Override
	protected void printNegativeInfinity(KdlNumber.NegativeInfinity negativeInfinity) {
		throw new UnsupportedOperationException("KDL v1 does not support Negative Infinity");
	}

	@Nullable
	@Override
	protected String escape(int c) {
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

	@Override
	protected boolean isValidStartOfIdentifier(int c) {
		return isIdentifierChar(c) && !isDecimalDigit(c);
	}
}
