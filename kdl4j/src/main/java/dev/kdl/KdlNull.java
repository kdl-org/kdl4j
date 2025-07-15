package dev.kdl;

import jakarta.annotation.Nullable;

/**
 * The KDL null value.
 *
 * @param type the type of the value
 */
public record KdlNull(@Nullable String type) implements KdlValue<Object> {
	/**
	 * Creates a new {@link KdlNull} with no type.
	 */
	public KdlNull() {
		this(null);
	}

	/**
	 * @return whether this value is a KDL null
	 */
	public boolean isNull() {
		return true;
	};

	@Override
	public Object value() {
		return null;
	}
}
