package dev.kdl.parse.lexer.token;

import dev.kdl.KdlNumber;
import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Token for numbers.
 */
public interface Number extends Token {
	/**
	 * Creates a KDL number from the current token.
	 *
	 * @param type the type of the KDL number
	 * @return a KDL number corresponding to this token
	 */
	KdlNumber<?> asKDLNumber(String type);

	/**
	 * @return the value of this number token as a {@link BigDecimal}
	 */
	BigDecimal asBigDecimal();

	/**
	 * Token for the positive infinity keyword.
	 *
	 * @param span the span of the token
	 */
	record PositiveInfinity(@Nonnull Span span) implements Number {
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

		/**
		 * The value of the positive infinity keyword.
		 */
		public static final String VALUE = "#inf";
	}

	/**
	 * Token for the negative infinity keyword.
	 *
	 * @param span the span of the token
	 */
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

		/**
		 * The value of the negative infinity keyword.
		 */
		public static final String VALUE = "#-inf";
	}

	/**
	 * Token for the not-a-number keyword.
	 *
	 * @param span the span of the token
	 */
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

		/**
		 * The value of the not-a-number keyword.
		 */
		public static final String VALUE = "#nan";
	}

	/**
	 * Token for an integer number.
	 *
	 * @param span the span of the token
	 */
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

	/**
	 * Token for a decimal number.
	 *
	 * @param span the span of the token
	 */
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

}
