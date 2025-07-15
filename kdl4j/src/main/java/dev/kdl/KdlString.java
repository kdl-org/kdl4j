package dev.kdl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * A KDL string value.
 *
 * @param type  the type of the value
 * @param value the value
 */
public record KdlString(@Nullable String type, @Nonnull String value) implements KdlValue<String> {
	/**
	 * Creates a new {@link KdlString} with no type.
	 *
	 * @param value the value
	 */
	public KdlString(@Nonnull String value) {
		this(null, value);
	}

	/**
	 * @return whether this value is a KDL string
	 */
	public boolean isString() {
		return true;
	}

	@Nonnull
	@Override
	public String toString() {
		return "String(" + value + ')';
	}
}
