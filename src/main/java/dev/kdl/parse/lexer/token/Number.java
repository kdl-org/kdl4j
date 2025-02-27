package dev.kdl.parse.lexer.token;

import dev.kdl.KdlNumber;
import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface Number extends Token {
	KdlNumber<?> asKDLNumber(String type);

	BigDecimal asBigDecimal();

	record Infinity(@Nonnull Span span) implements Number {
		@Nonnull
		@Override
		public String value() {
			return VALUE;
		}

		@Override
		public KdlNumber<?> asKDLNumber(String type) {
			return new KdlNumber.PositiveInfinity(type);
		}

		@Override
		public BigDecimal asBigDecimal() {
			throw new UnsupportedOperationException("Positive infinity cannot be converted to BigDecimal");
		}

		public static final String VALUE = "#inf";
	}

	record NegativeInfinity(@Nonnull Span span) implements Number {
		@Nonnull
		@Override
		public String value() {
			return VALUE;
		}


		@Override
		public KdlNumber<?> asKDLNumber(String type) {
			return new KdlNumber.NegativeInfinity(type);
		}

		@Override
		public BigDecimal asBigDecimal() {
			throw new UnsupportedOperationException("Negative infinity cannot be converted to BigDecimal");
		}

		public static final String VALUE = "#-inf";
	}

	record NaN(@Nonnull Span span) implements Number {
		@Nonnull
		@Override
		public String value() {
			return VALUE;
		}

		@Override
		public KdlNumber<?> asKDLNumber(String type) {
			return new KdlNumber.NotANumber(type);
		}

		@Override
		public BigDecimal asBigDecimal() {
			throw new UnsupportedOperationException("Not a number cannot be converted to BigDecimal");
		}

		public static final String VALUE = "#nan";
	}

	record Integer(@Nonnull BigInteger integer, @Nonnull Span span) implements Number {
		@Nonnull
		@Override
		public String value() {
			return integer.toString();
		}

		@Nonnull
		@Override
		public Span span() {
			return span;
		}

		@Override
		public KdlNumber<?> asKDLNumber(String type) {
			return new KdlNumber.Integer(type, integer);
		}

		@Override
		public BigDecimal asBigDecimal() {
			return new BigDecimal(integer);
		}
	}

	record Decimal(@Nonnull BigDecimal decimal, @Nonnull Span span) implements Number {
		@Nonnull
		@Override
		public String value() {
			return decimal.toString();
		}

		@Nonnull
		@Override
		public Span span() {
			return span;
		}

		@Override
		public KdlNumber<?> asKDLNumber(String type) {
			return new KdlNumber.Decimal(type, decimal);
		}

		@Override
		public BigDecimal asBigDecimal() {
			return decimal;
		}
	}

	static boolean isDecimalDigit(int c) {
		return c >= '0' && c <= '9';
	}

	static boolean isHexadecimalDigit(int c) {
		return isDecimalDigit(c) || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
	}

	static boolean isOctalDigit(int c) {
		return c >= '0' && c <= '7';
	}

	static boolean isBinaryDigit(int c) {
		return c == '0' || c == '1';
	}

	static boolean isSign(int c) {
		return c == '-' || c == '+';
	}
}
