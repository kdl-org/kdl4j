package dev.kdl;

import dev.kdl.parse.context.ParseContext;
import dev.kdl.parse.context.Span;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * A KDL property.
 *
 * @param name         the name of the property
 * @param value        the value of the property
 * @param nameSpan     the span of the property name, if any
 * @param parseContext the context in which the property was parsed, if any
 * @param <T>          the inner representation of the property's value
 */
public record KdlProperty<T>(
	@Nonnull String name,
	@Nonnull KdlValue<T> value,
	@Nullable Span nameSpan,
	@Nullable ParseContext parseContext
) {
	/**
	 * Creates a new KDL property without any location information.
	 *
	 * @param name  the name of the property
	 * @param value the value of the property
	 */
	public KdlProperty(@Nonnull String name, @Nonnull KdlValue<T> value) {
		this(name, value, null, null);
	}

	@Nonnull
	@Override
	public String toString() {
		return name + "=" + value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof KdlProperty<?> that)) {
			return false;
		}
		return name.equals(that.name) && value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}
}
