package kdl;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The properties of a {@link KDLNode}.
 */
public class Properties implements Iterable<Property<?>> {
	private Properties(Map<String, List<KDLValue<?>>> properties) {
		this.properties = Collections.unmodifiableMap(properties);
	}

	/**
	 * Retrieves the value of a property. If multiple values are defined for the property, the last one is returned.
	 *
	 * @param property the name of the property to retrieve.
	 * @param <T>      the type of the property's value
	 * @return an option containing the last value of the property if it has any
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	public <T> Optional<KDLValue<T>> getValue(@Nonnull String property) {
		var values = properties.get(property);
		return values == null || values.isEmpty()
			? Optional.empty()
			: Optional.of((KDLValue<T>) values.get(values.size() - 1));
	}

	@Nonnull
	@Override
	public Iterator<Property<?>> iterator() {
		return new PropertiesIterator();
	}

	private final Map<String, List<KDLValue<?>>> properties;

	/**
	 * @return a new properties builder.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A builder for {@link Properties}.
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
		public Builder property(String name, KDLValue<?> value) {
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
		public Properties build() {
			return new Properties(properties);
		}

		private final Map<String, List<KDLValue<?>>> properties = new HashMap<>();
	}

	private final class PropertiesIterator implements Iterator<Property<?>> {
		@Override
		public boolean hasNext() {
			return names.hasNext();
		}

		@Override
		public Property<?> next() {
			var name = names.next();
			var values = name == null ? null : properties.get(name);
			return values == null ? null : new Property<>(name, values.get(values.size() - 1));
		}

		private final Iterator<String> names = properties.keySet().stream().sorted().collect(Collectors.toList()).iterator();
	}
}
