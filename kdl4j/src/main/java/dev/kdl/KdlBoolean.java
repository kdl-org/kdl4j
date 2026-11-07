package dev.kdl;

import dev.kdl.parse.context.ParseContext;
import dev.kdl.parse.context.Span;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A KDL boolean value.
 *
 * @param type         the type of the value
 * @param booleanValue the value
 * @param typeSpan     the span of the type annotation, if any
 * @param parseContext the context in which the value was parsed, if any
 */
public record KdlBoolean(
	@Nullable String type,
	boolean booleanValue,
	@Nullable Span typeSpan,
	@Nullable ParseContext parseContext
) implements KdlValue<Boolean> {
	/**
	 * Creates a new {@link KdlBoolean} with no type.
	 *
	 * @param value the value
	 */
	public KdlBoolean(boolean value) {
		this(null, value, null, null);
	}

	/**
	 * Creates a new {@link KdlBoolean} with a type.
	 *
	 * @param type  the type of the value
	 * @param value the value
	 */
	public KdlBoolean(@Nullable String type, boolean value) {
		this(type, value, null, null);
	}

	/**
	 * @return whether this value is a KDL boolean
	 */
	public boolean isBoolean() {
		return true;
	}

	@Override
	public Boolean value() {
		return booleanValue;
	}

	@Override
	public boolean equals(Object o) {
		return this == o ||
			o instanceof KdlBoolean kdlBoolean
				&& booleanValue == kdlBoolean.booleanValue
				&& Objects.equals(type, kdlBoolean.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, booleanValue);
	}
}
