package dev.kdl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The properties of a {@link KdlNode}.
 */
public final class KdlProperties implements Iterable<KdlProperty<?>> {
	private KdlProperties(LinkedHashMap<String, List<KdlProperty<?>>> properties) {
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
	public <T> Optional<KdlValue<T>> getValue(String property) {
		var values = properties.get(property);
		return values == null || values.isEmpty()
			? Optional.empty()
			: Optional.of((KdlValue<T>) values.get(values.size() - 1).value());
	}

	/**
	 * Retrieves all the values of a property. Returns an empty list if the property is missing.
	 *
	 * @param property the name of the property to retrieve
	 * @return a list containing all the values of the property
	 */
	public List<KdlValue<?>> getValues(String property) {
		var values = properties.get(property);
		if (values == null) {
			return Collections.emptyList();
		}
		return values.stream().map(KdlProperty::value).collect(Collectors.toUnmodifiableList());
	}

	/**
	 * Retrieves all the properties with the provided name, in declaration order. Returns an empty list if the
	 * property is missing.
	 *
	 * @param property the name of the property to retrieve
	 * @return a list containing all the properties with the provided name
	 */
	public List<KdlProperty<?>> getProperties(String property) {
		var values = properties.get(property);
		return values == null ? Collections.emptyList() : Collections.unmodifiableList(values);
	}

	/**
	 * Checks if a property is present.
	 *
	 * @param property the name of the property to check
	 * @return {@code true} if the property is present, {@code false} otherwise
	 */
	public boolean hasProperty(String property) {
		return properties.containsKey(property);
	}

	@Override
	public Iterator<KdlProperty<?>> iterator() {
		return properties.values().stream()
			.flatMap(List::stream)
			.iterator();
	}

	/**
	 * @return all the properties, in declaration order
	 */
	public List<KdlProperty<?>> properties() {
		return properties.values().stream()
			.flatMap(List::stream)
			.toList();
	}

	/**
	 * @return a set containing the names of all the properties
	 */
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

	private final LinkedHashMap<String, List<KdlProperty<?>>> properties;

	/**
	 * @return a new {@link KdlProperties} builder.
	 */
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

		private Builder(Map<String, List<KdlProperty<?>>> properties) {
			this.properties = new LinkedHashMap<>();
			properties.forEach((name, values) -> this.properties.put(name, new ArrayList<>(values)));
		}

		/**
		 * Adds a property.
		 *
		 * @param name  the name of the property
		 * @param value the value of the property
		 * @return {@code this}
		 */
		public Builder property(String name, KdlValue<?> value) {
			return property(new KdlProperty<>(name, value));
		}

		/**
		 * Adds a property.
		 *
		 * @param property the property to add
		 * @return {@code this}
		 */
		public Builder property(KdlProperty<?> property) {
			properties.compute(property.name(), (k, values) -> {
				if (values == null) {
					values = new ArrayList<>();
				}
				values.add(property);
				return values;
			});
			return this;
		}

		/**
		 * Builds the properties
		 *
		 * @return the build properties
		 */
		public KdlProperties build() {
			return new KdlProperties(properties);
		}

		private final LinkedHashMap<String, List<KdlProperty<?>>> properties;
	}

}
