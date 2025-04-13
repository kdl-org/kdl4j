package dev.kdl;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The properties of a {@link KdlNode}.
 */
public final class KdlProperties implements Iterable<KdlProperty<?>> {
	private KdlProperties(@Nonnull Map<String, List<KdlValue<?>>> properties) {
		this.properties = Collections.unmodifiableMap(properties);
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

	public boolean hasProperty(@Nonnull String property) {
		return properties.containsKey(property);
	}

	@Nonnull
	@Override
	public Iterator<KdlProperty<?>> iterator() {
		return new PropertiesIterator();
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

	private final Map<String, List<KdlValue<?>>> properties;

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
		private final Map<String, List<KdlValue<?>>> properties = new HashMap<>();
	}

	private final class PropertiesIterator implements Iterator<KdlProperty<?>> {
		@Override
		public boolean hasNext() {
			return names.hasNext();
		}

		@Override
		public KdlProperty<?> next() {
			var name = names.next();
			var values = name == null ? null : properties.get(name);
			return values == null ? null : new KdlProperty<>(name, values.get(values.size() - 1));
		}

		private final Iterator<String> names = properties.keySet().stream().sorted().toList().iterator();
	}
}
