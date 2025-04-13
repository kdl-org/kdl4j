package dev.kdl.parse.lexer.helper;

import jakarta.annotation.Nonnull;

import java.util.function.Predicate;

public enum IntegerBase {
	BINARY, OCTAL, DECIMAL, HEXADECIMAL;

	public int radix() {
		return switch (this) {
			case BINARY -> 2;
			case OCTAL -> 8;
			case DECIMAL -> 10;
			case HEXADECIMAL -> 16;
		};
	}

	public Predicate<Integer> predicate() {
		return switch (this) {
			case BINARY -> KdlCharHelper::isBinaryDigit;
			case OCTAL -> KdlCharHelper::isOctalDigit;
			case DECIMAL -> KdlCharHelper::isDecimalDigit;
			case HEXADECIMAL -> KdlCharHelper::isHexadecimalDigit;
		};
	}

	@Nonnull
	public String getName() {
		return switch (this) {
			case BINARY -> "binary";
			case OCTAL -> "octal";
			case DECIMAL -> "decimal";
			case HEXADECIMAL -> "hexadecimal";
		};
	}

	@Nonnull
	public String getPrefix() {
		return switch (this) {
			case BINARY -> "0b";
			case OCTAL -> "0o";
			case DECIMAL -> "";
			case HEXADECIMAL -> "0x";
		};
	}
}
