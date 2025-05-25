package dev.kdl.parse.lexer.helper;

import jakarta.annotation.Nonnull;

import java.util.function.Predicate;

/**
 * A helper for parsing number. Represents a base for integer representation.
 */
public enum IntegerBase {
	/**
	 * Base 2
	 */
	BINARY,
	/**
	 * Base 8
	 */
	OCTAL,
	/**
	 * Base 10
	 */
	DECIMAL,
	/**
	 * Base  16
	 */
	HEXADECIMAL;

	/**
	 * The radix to use when parsing a number with this integer base.
	 *
	 * @return the radix of this base
	 */
	public int radix() {
		return switch (this) {
			case BINARY -> 2;
			case OCTAL -> 8;
			case DECIMAL -> 10;
			case HEXADECIMAL -> 16;
		};
	}

	/**
	 * A predicate that can check if a codepoint is a valid digit for the current integer base.
	 *
	 * @return a predicate for valid digits in this integer base
	 */
	@Nonnull
	public Predicate<Integer> predicate() {
		return switch (this) {
			case BINARY -> KdlCharHelper::isBinaryDigit;
			case OCTAL -> KdlCharHelper::isOctalDigit;
			case DECIMAL -> KdlCharHelper::isDecimalDigit;
			case HEXADECIMAL -> KdlCharHelper::isHexadecimalDigit;
		};
	}

	/**
	 * @return the name of this integer base
	 */
	@Nonnull
	public String getName() {
		return switch (this) {
			case BINARY -> "binary";
			case OCTAL -> "octal";
			case DECIMAL -> "decimal";
			case HEXADECIMAL -> "hexadecimal";
		};
	}

	/**
	 * @return the prefix to use when printing a number with this integer base
	 */
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
