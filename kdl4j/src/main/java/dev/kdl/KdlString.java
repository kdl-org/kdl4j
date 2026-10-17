package dev.kdl;

import dev.kdl.parse.context.ParseContext;
import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * A KDL string value.
 *
 * @param type         the type of the value
 * @param value        the value
 * @param typeSpan     the span of the type annotation, if any
 * @param parseContext the context in which the value was parsed, if any
 */
public record KdlString(
	@Nullable String type,
	@Nonnull String value,
	@Nullable Span typeSpan,
	@Nullable ParseContext parseContext
) implements KdlValue<String> {
	/**
	 * Creates a new {@link KdlString} with no type.
	 *
	 * @param value the value
	 */
	public KdlString(@Nonnull String value) {
		this(null, value, null, null);
	}

	/**
	 * Creates a new {@link KdlString} with a type.
	 *
	 * @param type  the type of the value
	 * @param value the value
	 */
	public KdlString(@Nullable String type, @Nonnull String value) {
		this(type, value, null, null);
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof KdlString that)) {
			return false;
		}
		return Objects.equals(type, that.type) && value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, value);
	}
}
