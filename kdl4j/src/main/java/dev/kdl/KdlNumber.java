package dev.kdl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;

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
	@Nonnull
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
	@Nonnull
	BigDecimal asBigDecimal();

	/**
	 * @return whether this value is a KDL number
	 */
	default boolean isNumber() {
		return true;
	};

	/**
	 * Creates a {@link KdlNumber} from a {@link Number}.
	 *
	 * @param number the number to represent
	 * @return the corresponding {@link KdlNumber}
	 */
	@Nonnull
	static KdlNumber<?> from(@Nonnull Number number) {
		return from(null, number);
	}

	/**
	 * Creates a {@link KdlNumber} from a {@link Number}.
	 *
	 * @param type   the type of the KDL number
	 * @param number the number to represent
	 * @return the corresponding {@link KdlNumber}
	 */
	@Nonnull
	static KdlNumber<?> from(@Nullable String type, @Nonnull Number number) {
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
	 * @param type the type of the value
	 */
	record NotANumber(String type) implements KdlNumber<Double> {
		/**
		 * Creates a new {@link NotANumber} with no type.
		 */
		public NotANumber() {
			this(null);
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

		@Nonnull
		@Override
		public BigInteger asBigInteger() {
			throw new UnsupportedOperationException("Not a number cannot be converted to BigInteger");
		}

		@Override
		public double asDouble() {
			return Double.NaN;
		}

		@Nonnull
		@Override
		public BigDecimal asBigDecimal() {
			throw new UnsupportedOperationException("Not a number cannot be converted to BigDecimal");
		}

		@Nonnull
		@Override
		public String toString() {
			return "NaN";
		}
	}

	/**
	 * The KDL positive infinity value.
	 *
	 * @param type the type of the value
	 */
	record PositiveInfinity(@Nullable String type) implements KdlNumber<Double> {
		/**
		 * Creates a new {@link PositiveInfinity} with no type.
		 */
		public PositiveInfinity() {
			this(null);
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

		@Nonnull
		@Override
		public BigInteger asBigInteger() {
			throw new UnsupportedOperationException("Positive infinity cannot be converted to BigInteger");
		}

		@Override
		public double asDouble() {
			return Double.POSITIVE_INFINITY;
		}

		@Nonnull
		@Override
		public BigDecimal asBigDecimal() {
			throw new UnsupportedOperationException("Positive infinity cannot be converted to BigDecimal");
		}

		@Nonnull
		@Override
		public String toString() {
			return "+inf";
		}
	}

	/**
	 * The negative infinity KDL value.
	 *
	 * @param type the type of the value
	 */
	record NegativeInfinity(@Nullable String type) implements KdlNumber<Double> {
		/**
		 * Creates a new {@link NegativeInfinity} with no type.
		 */
		public NegativeInfinity() {
			this(null);
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

		@Nonnull
		@Override
		public BigInteger asBigInteger() {
			throw new UnsupportedOperationException("Negative infinity cannot be converted to BigInteger");
		}

		@Override
		public double asDouble() {
			return Double.NEGATIVE_INFINITY;
		}

		@Nonnull
		@Override
		public BigDecimal asBigDecimal() {
			throw new UnsupportedOperationException("Negative infinity cannot be converted to BigDecimal");
		}

		@Nonnull
		@Override
		public String toString() {
			return "-inf";
		}
	}

	/**
	 * A KDL number representing an integer.
	 *
	 * @param type  the type of the value
	 * @param value the integer value
	 */
	record Integer(@Nullable String type, @Nonnull BigInteger value) implements KdlNumber<BigInteger> {
		/**
		 * Creates a new {@link Integer} with no type.
		 *
		 * @param value the integer value
		 */
		public Integer(@Nonnull BigInteger value) {
			this(null, value);
		}

		@Nonnull
		@Override
		public BigInteger asBigInteger() {
			return value;
		}

		@Nonnull
		@Override
		public BigDecimal asBigDecimal() {
			return new BigDecimal(value);
		}

		@Nonnull
		@Override
		public String toString() {
			return "Integer(" + value + ')';
		}
	}

	/**
	 * A KDL number representing a decimal number.
	 *
	 * @param type  the type of the value
	 * @param value the integer value
	 */
	record Decimal(@Nullable String type, @Nonnull BigDecimal value) implements KdlNumber<BigDecimal> {
		/**
		 * Creates a new {@link Decimal} with no type.
		 *
		 * @param value the integer value
		 */
		public Decimal(@Nonnull BigDecimal value) {
			this(null, value);
		}

		@Nonnull
		@Override
		public BigInteger asBigInteger() {
			return value.toBigInteger();
		}

		@Nonnull
		@Override
		public BigDecimal asBigDecimal() {
			return value;
		}

		@Nonnull
		@Override
		public String toString() {
			return "Decimal(" + value + ')';
		}
	}
}
