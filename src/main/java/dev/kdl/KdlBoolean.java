package dev.kdl;

import jakarta.annotation.Nullable;

/**
 * A KDL boolean value.
 *
 * @param type         the type of the value
 * @param booleanValue the value
 */
public record KdlBoolean(@Nullable String type, boolean booleanValue) implements KdlValue<Boolean> {
	/**
	 * Creates a new {@link KdlBoolean} with no type.
	 *
	 * @param value the value
	 */
	public KdlBoolean(boolean value) {
		this(null, value);
	}

	@Override
	public Boolean value() {
		return booleanValue;
	}
}
