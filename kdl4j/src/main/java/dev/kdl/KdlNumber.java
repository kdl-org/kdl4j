package dev.kdl;

import dev.kdl.parse.context.ParseContext;
import dev.kdl.parse.context.Span;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Supertype for all KDL number values.
 *
 * @param <T> the inner representation of the number value
 */
public interface KdlNumber<T extends Number> extends KdlValue<T> {
	/**
	 * @return the number converted to a byte
	 */
	default byte asByte() {
		return value().byteValue();
	}

	/**
	 * @return the number converted to an int
	 */
	default int asInt() {
		return value().intValue();
	}

	/**
	 * @return the number converted to a long
	 */
	default long asLong() {
		return value().longValue();
	}

	/**
	 * @return the number converted to a {@link BigInteger}
	 */
	BigInteger asBigInteger();

	/**
	 * @return the number converted to a double
	 */
	default double asDouble() {
		return value().longValue();
	}

	/**
	 * @return the number converted to a {@link BigDecimal}
	 */
	BigDecimal asBigDecimal();

	/**
	 * @return whether this value is a KDL number
	 */
	default boolean isNumber() {
		return true;
	}

	/**
	 * Creates a {@link KdlNumber} from a {@link Number}.
	 *
	 * @param number the number to represent
	 * @return the corresponding {@link KdlNumber}
	 */
	static KdlNumber<?> from(Number number) {
		return from(null, number);
	}

	/**
	 * Creates a {@link KdlNumber} from a {@link Number}.
	 *
	 * @param type   the type of the KDL number
	 * @param number the number to represent
	 * @return the corresponding {@link KdlNumber}
	 */
	static KdlNumber<?> from(@Nullable String type, Number number) {
		if (number instanceof BigInteger) {
			return new Integer(type, (BigInteger) number);
		} else if (number instanceof BigDecimal) {
			return new Decimal(type, (BigDecimal) number);
		} else if (number instanceof Byte || number instanceof Short || number instanceof java.lang.Integer || number instanceof Long) {
			return new Integer(type, BigInteger.valueOf(number.longValue()));
		}
		return new Decimal(type, new BigDecimal(number.toString()));
	}

	/**
	 * The KDL Not-a-number value.
	 *
	 * @param type         the type of the value
	 * @param typeSpan     the span of the type annotation, if any
	 * @param parseContext the context in which the value was parsed, if any
	 */
	record NotANumber(@Nullable String type, @Nullable Span typeSpan, @Nullable ParseContext parseContext) implements KdlNumber<Double> {
		/**
		 * Creates a new {@link NotANumber} with no type.
		 */
		public NotANumber() {
			this(null, null, null);
		}

		/**
		 * Creates a new {@link NotANumber} with a type.
		 *
		 * @param type the type of the value
		 */
		public NotANumber(@Nullable String type) {
			this(type, null, null);
		}

		@Override
		public Double value() {
			return Double.NaN;
		}

		@Override
		public byte asByte() {
			throw new UnsupportedOperationException("Not a number cannot be converted to byte");
		}

		@Override
		public int asInt() {
			throw new UnsupportedOperationException("Not a number cannot be converted to int");
		}

		@Override
		public long asLong() {
			throw new UnsupportedOperationException("Not a number cannot be converted to long");
		}

		@Override
		public BigInteger asBigInteger() {
			throw new UnsupportedOperationException("Not a number cannot be converted to BigInteger");
		}

		@Override
		public double asDouble() {
			return Double.NaN;
		}

		@Override
		public BigDecimal asBigDecimal() {
			throw new UnsupportedOperationException("Not a number cannot be converted to BigDecimal");
		}

		@Override
		public String toString() {
			return "NaN";
		}

		@Override
		public boolean equals(Object o) {
			return this == o || o instanceof NotANumber notANumber && Objects.equals(type, notANumber.type);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(type);
		}
	}

	/**
	 * The KDL positive infinity value.
	 *
	 * @param type         the type of the value
	 * @param typeSpan     the span of the type annotation, if any
	 * @param parseContext the context in which the value was parsed, if any
	 */
	record PositiveInfinity(@Nullable String type, @Nullable Span typeSpan, @Nullable ParseContext parseContext) implements KdlNumber<Double> {
		/**
		 * Creates a new {@link PositiveInfinity} with no type.
		 */
		public PositiveInfinity() {
			this(null, null, null);
		}

		/**
		 * Creates a new {@link PositiveInfinity} with a type.
		 *
		 * @param type the type of the value
		 */
		public PositiveInfinity(@Nullable String type) {
			this(type, null, null);
		}

		@Override
		public Double value() {
			return Double.POSITIVE_INFINITY;
		}

		@Override
		public byte asByte() {
			throw new UnsupportedOperationException("Positive infinity cannot be converted to byte");
		}

		@Override
		public int asInt() {
			throw new UnsupportedOperationException("Positive infinity cannot be converted to int");
		}

		@Override
		public long asLong() {
			throw new UnsupportedOperationException("Positive infinity cannot be converted to long");
		}

		@Override
		public BigInteger asBigInteger() {
			throw new UnsupportedOperationException("Positive infinity cannot be converted to BigInteger");
		}

		@Override
		public double asDouble() {
			return Double.POSITIVE_INFINITY;
		}

		@Override
		public BigDecimal asBigDecimal() {
			throw new UnsupportedOperationException("Positive infinity cannot be converted to BigDecimal");
		}

		@Override
		public String toString() {
			return "+inf";
		}

		@Override
		public boolean equals(Object o) {
			return this == o || o instanceof PositiveInfinity positiveInfinity && Objects.equals(type, positiveInfinity.type);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(type);
		}
	}

	/**
	 * The negative infinity KDL value.
	 *
	 * @param type         the type of the value
	 * @param typeSpan     the span of the type annotation, if any
	 * @param parseContext the context in which the value was parsed, if any
	 */
	record NegativeInfinity(@Nullable String type, @Nullable Span typeSpan, @Nullable ParseContext parseContext) implements KdlNumber<Double> {
		/**
		 * Creates a new {@link NegativeInfinity} with no type.
		 */
		public NegativeInfinity() {
			this(null, null, null);
		}

		/**
		 * Creates a new {@link NegativeInfinity} with a type.
		 *
		 * @param type the type of the value
		 */
		public NegativeInfinity(@Nullable String type) {
			this(type, null, null);
		}

		@Override
		public Double value() {
			return Double.NEGATIVE_INFINITY;
		}

		@Override
		public byte asByte() {
			throw new UnsupportedOperationException("Negative infinity cannot be converted to byte");
		}

		@Override
		public int asInt() {
			throw new UnsupportedOperationException("Negative infinity cannot be converted to int");
		}

		@Override
		public long asLong() {
			throw new UnsupportedOperationException("Negative infinity cannot be converted to long");
		}

		@Override
		public BigInteger asBigInteger() {
			throw new UnsupportedOperationException("Negative infinity cannot be converted to BigInteger");
		}

		@Override
		public double asDouble() {
			return Double.NEGATIVE_INFINITY;
		}

		@Override
		public BigDecimal asBigDecimal() {
			throw new UnsupportedOperationException("Negative infinity cannot be converted to BigDecimal");
		}

		@Override
		public String toString() {
			return "-inf";
		}

		@Override
		public boolean equals(Object o) {
			return this == o || o instanceof NegativeInfinity negativeInfinity && Objects.equals(type, negativeInfinity.type);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(type);
		}
	}

	/**
	 * A KDL number representing an integer.
	 *
	 * @param type         the type of the value
	 * @param value        the integer value
	 * @param typeSpan     the span of the type annotation, if any
	 * @param parseContext the context in which the value was parsed, if any
	 */
	record Integer(@Nullable String type, BigInteger value, @Nullable Span typeSpan, @Nullable ParseContext parseContext) implements KdlNumber<BigInteger> {
		/**
		 * Creates a new {@link Integer} with no type.
		 *
		 * @param value the integer value
		 */
		public Integer(BigInteger value) {
			this(null, value, null, null);
		}

		/**
		 * Creates a new {@link Integer} with a type.
		 *
		 * @param type  the type of the value
		 * @param value the integer value
		 */
		public Integer(@Nullable String type, BigInteger value) {
			this(type, value, null, null);
		}

		@Override
		public BigInteger asBigInteger() {
			return value;
		}

		@Override
		public BigDecimal asBigDecimal() {
			return new BigDecimal(value);
		}

		@Override
		public String toString() {
			return "Integer(" + value + ')';
		}

		@Override
		public boolean equals(Object o) {
			return this == o ||
				o instanceof Integer integer
					&& Objects.equals(type, integer.type)
					&& value.equals(integer.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(type, value);
		}
	}

	/**
	 * A KDL number representing a decimal number.
	 *
	 * @param type         the type of the value
	 * @param value        the integer value
	 * @param typeSpan     the span of the type annotation, if any
	 * @param parseContext the context in which the value was parsed, if any
	 */
	record Decimal(@Nullable String type, BigDecimal value, @Nullable Span typeSpan, @Nullable ParseContext parseContext) implements KdlNumber<BigDecimal> {
		/**
		 * Creates a new {@link Decimal} with no type.
		 *
		 * @param value the integer value
		 */
		public Decimal(BigDecimal value) {
			this(null, value, null, null);
		}

		/**
		 * Creates a new {@link Decimal} with a type.
		 *
		 * @param type  the type of the value
		 * @param value the integer value
		 */
		public Decimal(@Nullable String type, BigDecimal value) {
			this(type, value, null, null);
		}

		@Override
		public BigInteger asBigInteger() {
			return value.toBigInteger();
		}

		@Override
		public BigDecimal asBigDecimal() {
			return value;
		}

		@Override
		public String toString() {
			return "Decimal(" + value + ')';
		}

		@Override
		public boolean equals(Object o) {
			return this == o ||
				o instanceof Decimal decimal
					&& Objects.equals(type, decimal.type)
					&& value.equals(decimal.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(type, value);
		}
	}
}
