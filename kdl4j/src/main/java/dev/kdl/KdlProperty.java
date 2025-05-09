package dev.kdl;

import jakarta.annotation.Nonnull;

public record KdlProperty<T>(@Nonnull String name, @Nonnull KdlValue<T> value) {
	@Nonnull
	@Override
	public String toString() {
		return name + "=" + value;
	}
}
