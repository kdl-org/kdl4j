package kdl;

import jakarta.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

/**
 * Supertype for all KDL number values.
 *
 * @param <T> the inner representation of the number value
 */
public abstract class KDLNumber<T extends Number> extends KDLValue<T> {
	KDLNumber(@Nonnull Optional<String> type) {
		super(type);
	}

	/**
	 * @return the number converted to a byte
	 */
	public byte asByte() {
		return getValue().byteValue();
	}

	/**
	 * @return the number converted to an int
	 */
	public int asInt() {
		return getValue().intValue();
	}

	/**
	 * @return the number converted to a long
	 */
	public long asLong() {
		return getValue().longValue();
	}

	/**
	 * @return the number converted to a {@link BigInteger}
	 */
	@Nonnull
	public abstract BigInteger asBigInteger();

	/**
	 * @return the number converted to a double
	 */
	public double asDouble() {
		return getValue().longValue();
	}

	/**
	 * @return the number converted to a {@link BigDecimal}
	 */
	@Nonnull
	public abstract BigDecimal asBigDecimal();

	/**
	 * Creates a {@link KDLNumber} from a {@link Number}.
	 *
	 * @param number the number to represent
	 * @return the corresponding {@link KDLNumber}
	 */
	@Nonnull
	public static KDLNumber<?> from(@Nonnull Number number) {
		return from(Optional.empty(), number);
	}

	/**
	 * Creates a {@link KDLNumber} from a {@link Number}.
	 *
	 * @param type   the type of the KDL number
	 * @param number the number to represent
	 * @return the corresponding {@link KDLNumber}
	 */
	@Nonnull
	public static KDLNumber<?> from(@Nonnull Optional<String> type, @Nonnull Number number) {
		if (number instanceof BigInteger) {
			return new KDLNumber.Integer(type, (BigInteger) number);
		} else if (number instanceof BigDecimal) {
			return new KDLNumber.Decimal(type, (BigDecimal) number);
		} else if (number instanceof Byte || number instanceof Short || number instanceof java.lang.Integer || number instanceof Long) {
			return new KDLNumber.Integer(type, BigInteger.valueOf(number.longValue()));
		}
		return new KDLNumber.Decimal(type, new BigDecimal(number.toString()));
	}

	/**
	 * The KDL Not-a-number value.
	 */
	public static final class NotANumber extends KDLNumber<Double> {
		/**
		 * Creates a new {@link NotANumber}.
		 *
		 * @param type the type of the value
		 */
		public NotANumber(@Nonnull Optional<String> type) {
			super(type);
		}

		@Override
		public Double getValue() {
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

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			var that = (NotANumber) o;
			return Objects.equals(type, that.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(type);
		}

		@Override
		public String toString() {
			return "NaN";
		}
	}

	/**
	 * The KDL positive infinity value.
	 */
	public static final class PositiveInfinity extends KDLNumber<Double> {
		/**
		 * Creates a new {@link PositiveInfinity}.
		 *
		 * @param type the type of the value
		 */
		public PositiveInfinity(@Nonnull Optional<String> type) {
			super(type);
		}

		@Override
		public Double getValue() {
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

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			var that = (PositiveInfinity) o;
			return Objects.equals(type, that.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(type);
		}

		@Override
		public String toString() {
			return "+inf";
		}
	}

	/**
	 * The negative infinity KDL value.
	 */
	public static final class NegativeInfinity extends KDLNumber<Double> {
		/**
		 * Creates a new {@link NegativeInfinity}.
		 *
		 * @param type the type of the value
		 */
		public NegativeInfinity(@Nonnull Optional<String> type) {
			super(type);
		}

		@Override
		public Double getValue() {
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

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			var that = (NegativeInfinity) o;
			return Objects.equals(type, that.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(type);
		}

		@Override
		public String toString() {
			return "-inf";
		}
	}

	/**
	 * A KDL number representing an integer.
	 */
	public static final class Integer extends KDLNumber<BigInteger> {
		/**
		 * Creates a new {@link Integer}.
		 *
		 * @param type  the type of the value
		 * @param value the integer value
		 */
		public Integer(@Nonnull Optional<String> type, @Nonnull BigInteger value) {
			super(type);
			this.value = value;
		}

		@Override
		@Nonnull
		public BigInteger getValue() {
			return value;
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

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			var that = (Integer) o;
			return Objects.equals(value, that.value) && Objects.equals(type, that.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value, type);
		}

		@Override
		public String toString() {
			return "Integer(" + value + ')';
		}

		@Nonnull
		private final BigInteger value;
	}

	/**
	 * A KDL number representing a decimal number.
	 */
	public static final class Decimal extends KDLNumber<BigDecimal> {
		/**
		 * Creates a new {@link Decimal}.
		 *
		 * @param type  the type of the value
		 * @param value the integer value
		 */
		public Decimal(@Nonnull Optional<String> type, @Nonnull BigDecimal value) {
			super(type);
			this.value = value;
		}

		@Override
		@Nonnull
		public BigDecimal getValue() {
			return value;
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

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			var that = (Decimal) o;
			return Objects.equals(value, that.value) && Objects.equals(type, that.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value, type);
		}

		@Override
		public String toString() {
			return "Decimal(" + value + ')';
		}

		@Nonnull
		private final BigDecimal value;
	}
}
