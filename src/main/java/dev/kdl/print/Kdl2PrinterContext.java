package dev.kdl.print;

import dev.kdl.KdlBoolean;
import dev.kdl.KdlNull;
import dev.kdl.KdlNumber;
import dev.kdl.parse.lexer.helper.Kdl2CharHelper;
import jakarta.annotation.Nullable;

import java.io.Writer;

import static dev.kdl.parse.lexer.helper.Kdl2CharHelper.isUnambiguousIdentifierChar;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.isSign;

public class Kdl2PrinterContext extends KdlPrinterContext {
	Kdl2PrinterContext(Writer writer, KdlPrinterConfiguration configuration) {
		super(writer, configuration);
	}

	@Override
	protected boolean isIdentifierChar(int c) {
		return Kdl2CharHelper.isIdentifierChar(c);
	}

	@Override
	protected void printNull(KdlNull kdlNull) {
		printType(kdlNull.type());
		write("#null");
	}

	@Override
	protected void printBoolean(KdlBoolean kdlBoolean) {
		printType(kdlBoolean.type());
		write(kdlBoolean.value() ? "#true" : "#false");
	}

	@Override
	protected void printNotANumber(KdlNumber.NotANumber notANumber) {
		printType(notANumber.type());
		write("#nan");
	}

	@Override
	protected void printPositiveInfinity(KdlNumber.PositiveInfinity positiveInfinity) {
		printType(positiveInfinity.type());
		write("#inf");
	}

	@Override
	protected void printNegativeInfinity(KdlNumber.NegativeInfinity negativeInfinity) {
		printType(negativeInfinity.type());
		write("#-inf");
	}

	@Override
	protected boolean isValidStartOfIdentifier(int c) {
		return isSign(c) || c == '.' || isUnambiguousIdentifierChar(c);
	}

	@Override
	@Nullable
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
}
