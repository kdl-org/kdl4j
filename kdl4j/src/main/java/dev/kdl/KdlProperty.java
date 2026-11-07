package dev.kdl;

import dev.kdl.parse.context.ParseContext;
import dev.kdl.parse.context.Span;
import org.jspecify.annotations.Nullable;

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
	String name,
	KdlValue<T> value,
	@Nullable Span nameSpan,
	@Nullable ParseContext parseContext
) {
	/**
	 * Creates a new KDL property without any location information.
	 *
	 * @param name  the name of the property
	 * @param value the value of the property
	 */
	public KdlProperty(String name, KdlValue<T> value) {
		this(name, value, null, null);
	}

	@Override
	public String toString() {
		return name + "=" + value;
	}

	@Override
	public boolean equals(Object o) {
		return this == o ||
			o instanceof KdlProperty<?> kdlProperty
				&& name.equals(kdlProperty.name)
				&& value.equals(kdlProperty.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}
}
