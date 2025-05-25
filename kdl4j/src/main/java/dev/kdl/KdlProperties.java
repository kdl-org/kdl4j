package dev.kdl;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * The properties of a {@link KdlNode}.
 */
public final class KdlProperties implements Iterable<Entry<String, List<KdlValue<?>>>> {
	private KdlProperties(@Nonnull LinkedHashMap<String, List<KdlValue<?>>> properties) {
		this.properties = properties;
	}

	/**
	 * Retrieves the value of a property. If multiple values are defined for the property, the last one is returned.
	 *
	 * @param property the name of the property to retrieve
	 * @param <T>      the type of the property's value
	 * @return an option containing the last value of the property if it has any
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	public <T> Optional<KdlValue<T>> getValue(@Nonnull String property) {
		var values = properties.get(property);
		return values == null || values.isEmpty()
			? Optional.empty()
			: Optional.of((KdlValue<T>) values.get(values.size() - 1));
	}

	/**
	 * Retrieves all the values of a property. Returns an empty list if the property is missing.
	 *
	 * @param property the name of the property to retrieve
	 * @return a list containing all the values of the property
	 */
	@Nonnull
	public List<KdlValue<?>> getValues(@Nonnull String property) {
		var values = properties.get(property);
		return values == null ? Collections.emptyList() : Collections.unmodifiableList(values);
	}

	/**
	 * Checks if a property is present.
	 *
	 * @param property the name of the property to check
	 * @return {@code true} if the property is present, {@code false} otherwise
	 */
	public boolean hasProperty(@Nonnull String property) {
		return properties.containsKey(property);
	}

	@Nonnull
	@Override
	public Iterator<Entry<String, List<KdlValue<?>>>> iterator() {
		return properties.entrySet().iterator();
	}

	/**
	 * @return a set containing the names of all the properties
	 */
	@Nonnull
	public Set<String> propertyNames() {
		return properties.keySet();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof KdlProperties that) {
			return Objects.equals(properties, that.properties);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(properties);
	}

	/**
	 * Creates a new builder to create new properties from the current ones.
	 *
	 * @return a new builder with the current properties
	 */
	public Builder mutate() {
		return new Builder(properties);
	}

	private final LinkedHashMap<String, List<KdlValue<?>>> properties;

	/**
	 * @return a new {@link KdlProperties} builder.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A builder for {@link KdlProperties}.
	 */
	public static final class Builder {
		private Builder() {
			this.properties = new LinkedHashMap<>();
		}

		private Builder(@Nonnull Map<String, List<KdlValue<?>>> properties) {
			this.properties = new LinkedHashMap<>(properties);
		}

		/**
		 * Adds a property.
		 *
		 * @param name  the name of the property
		 * @param value the value of the property
		 * @return {@code this}
		 */
		@Nonnull
		public Builder property(String name, KdlValue<?> value) {
			properties.compute(name, (k, values) -> {
				if (values == null) {
					values = new ArrayList<>();
				}
				values.add(value);
				return values;
			});
			return this;
		}

		/**
		 * Builds the properties
		 *
		 * @return the build properties
		 */
		@Nonnull
		public KdlProperties build() {
			return new KdlProperties(properties);
		}

		@Nonnull
		private final LinkedHashMap<String, List<KdlValue<?>>> properties;
	}

}
