package kdl;

import jakarta.annotation.Nonnull;

public class Property<T> {
	public Property(@Nonnull String name, @Nonnull KDLValue<T> value) {
		this.name = name;
		this.value = value;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	@Nonnull
	public KDLValue<T> getValue() {
		return value;
	}

	@Nonnull
	private final String name;
	@Nonnull
	private final KDLValue<T> value;
}
