package kdl.parse.lexer.token;

import jakarta.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import kdl.KDLNumber;
import kdl.parse.lexer.Token;

public interface Number extends Token {
	KDLNumber<?> asKDLNumber(Optional<String> type);

	class Infinity implements Number {
		private Infinity() {
		}

		@Override
		@Nonnull
		public String value() {
			return value;
		}

		@Override
		public String toString() {
			return value;
		}

		@Override
		public KDLNumber<?> asKDLNumber(Optional<String> type) {
			return new KDLNumber.PositiveInfinity(type);
		}

		public final String value = "#inf";
	}

	class MinusInfinity implements Number {
		private MinusInfinity() {
		}

		@Override
		@Nonnull
		public String value() {
			return value;
		}

		@Override
		public String toString() {
			return value;
		}

		@Override
		public KDLNumber<?> asKDLNumber(Optional<String> type) {
			return new KDLNumber.NegativeInfinity(type);
		}

		public final String value = "#-inf";
	}

	class NaN implements Number {
		private NaN() {
		}

		@Override
		@Nonnull
		public String value() {
			return value;
		}

		@Override
		public String toString() {
			return value;
		}

		@Override
		public KDLNumber<?> asKDLNumber(Optional<String> type) {
			return new KDLNumber.NotANumber(type);
		}

		public final String value = "#nan";

	}

	class Integer implements Number {
		public Integer(@Nonnull BigInteger value) {
			this.value = value;
		}

		@Override
		@Nonnull
		public String value() {
			return value.toString();
		}

		@Override
		public KDLNumber<?> asKDLNumber(Optional<String> type) {
			return new KDLNumber.Integer(type, value);
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) return true;
			if (object == null || getClass() != object.getClass()) return false;
			Integer integer = (Integer) object;
			return Objects.equals(value, integer.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public String toString() {
			return "Integer(" + value + ')';
		}

		@Nonnull
		private final BigInteger value;

	}

	class Decimal implements Number {
		public Decimal(@Nonnull BigDecimal value) {
			this.value = value;
		}

		@Override
		@Nonnull
		public String value() {
			return value.toString();
		}

		@Override
		public KDLNumber<?> asKDLNumber(Optional<String> type) {
			return new KDLNumber.Decimal(type, value);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			var decimal = (Decimal) o;
			return Objects.equals(value, decimal.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public String toString() {
			return "Decimal(" + value + ")";
		}

		@Nonnull
		private final BigDecimal value;

	}

	Infinity INFINITY = new Infinity();
	MinusInfinity MINUS_INFINITY = new MinusInfinity();
	NaN NAN = new NaN();

	static boolean isDigit(int c) {
		return c >= '0' && c <= '9';
	}

	static boolean isHexDigit(int c) {
		return isDigit(c) || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
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
