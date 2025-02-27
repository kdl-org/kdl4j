package dev.kdl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Supertype for all KDL values.
 *
 * @param <T> the inner representation of the value.
 */
public interface KdlValue<T> {

	/**
	 * @return the type of the value or <code>null</code> if there is no type
	 */
	@Nullable
	String type();

	/**
	 * @return the value
	 */
	T value();

	/**
	 * @return whether this value is a KDL string
	 */
	default boolean isString() {
		return false;
	}

	/**
	 * @return whether this value is a KDL number
	 */
	default boolean isNumber() {
		return false;
	}

	/**
	 * @return whether this value is a KDL boolean
	 */
	default boolean isBoolean() {
		return false;
	}

	/**
	 * @return whether this value is a KDL null
	 */
	default boolean isNull() {
		return false;
	}

	/**
	 * Creates a new KDL value from its representation.
	 *
	 * @param value the value to wrap in a {@link KdlValue}
	 * @return a corresponding {@link KdlValue}
	 */
	@Nonnull
	static KdlValue<?> from(@Nullable Object value) {
		if (value instanceof KdlValue<?>) {
			return (KdlValue<?>) value;
		}
		return from(null, value);
	}

	/**
	 * Creates a new KDL value from its representation.
	 *
	 * @param type  the type of the value
	 * @param value the value to wrap in a {@link KdlValue}
	 * @return a corresponding {@link KdlValue}
	 */
	@Nonnull
	static KdlValue<?> from(@Nullable String type, @Nullable Object value) {
		if (value == null) {
			return new KdlNull(type);
		} else if (value instanceof KdlValue<?>) {
			return (KdlValue<?>) value;
		} else if (value instanceof Boolean) {
			return new KdlBoolean(type, (Boolean) value);
		} else if (value instanceof Number) {
			return KdlNumber.from(type, (Number) value);
		} else if (value instanceof String) {
			return new KdlString(type, (String) value);
		}

		throw new IllegalArgumentException("Could not convert " + value + " to a KDL value");
	}

}
