package dev.kdl;

import jakarta.annotation.Nonnull;

/**
 * A KDL property.
 *
 * @param name  the name of the property
 * @param value the value of the property
 * @param <T>   the inner representation of the property's value
 */
public record KdlProperty<T>(@Nonnull String name, @Nonnull KdlValue<T> value) {
	@Nonnull
	@Override
	public String toString() {
		return name + "=" + value;
	}
}
