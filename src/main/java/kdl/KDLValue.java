package kdl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Optional;

/**
 * Supertype for all KDL values.
 *
 * @param <T> the inner representation of the value.
 */
public abstract class KDLValue<T> {
	KDLValue(@Nonnull Optional<String> type) {
		this.type = type;
	}

	/**
	 * @return the type of the value
	 */
	@Nonnull
	public Optional<String> getType() {
		return type;
	}

	/**
	 * @return the value
	 */
	public abstract T getValue();

	/**
	 * @return whether this value is a KDL string
	 */
	public boolean isString() {
		return false;
	}

	/**
	 * @return whether this value is a KDL number
	 */
	public boolean isNumber() {
		return false;
	}

	/**
	 * @return whether this value is a KDL boolean
	 */
	public boolean isBoolean() {
		return false;
	}

	/**
	 * @return whether this value is a KDL null
	 */
	public boolean isNull() {
		return false;
	}

	@Nonnull
	protected final Optional<String> type;

	/**
	 * Creates a new KDL value from its representation.
	 *
	 * @param value the value to wrap in a {@link KDLValue}
	 * @return a corresponding {@link KDLValue}
	 */
	public static KDLValue<?> from(Object value) {
		if (value instanceof KDLValue<?>) {
			return (KDLValue<?>) value;
		}
		return from(Optional.empty(), value);
	}

	/**
	 * Creates a new KDL value from its representation.
	 *
	 * @param type  the type of the value
	 * @param value the value to wrap in a {@link KDLValue}
	 * @return a corresponding {@link KDLValue}
	 */
	@Nonnull
	public static KDLValue<?> from(@Nonnull Optional<String> type, @Nullable Object value) {
		if (value == null) {
			return new KDLNull(type);
		} else if (value instanceof KDLValue<?>) {
			return (KDLValue<?>) value;
		} else if (value instanceof Boolean) {
			return new KDLBoolean(type, (Boolean) value);
		} else if (value instanceof Number) {
			return KDLNumber.from(type, (Number) value);
		} else if (value instanceof String) {
			return new KDLString(type, (String) value);
		}

		throw new IllegalArgumentException("Could not convert " + value + " to a KDL value");
	}

}
