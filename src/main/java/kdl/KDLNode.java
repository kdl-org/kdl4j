package kdl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A KDL node.
 */
public class KDLNode {
	/**
	 * Creates a new KDL node.
	 *
	 * @param type       the type of the node
	 * @param name       the name of the node
	 * @param arguments  the arguments of the node
	 * @param properties the properties of the node
	 * @param children   the children of the node
	 */
	public KDLNode(
		@Nonnull Optional<String> type,
		@Nonnull String name,
		@Nonnull List<KDLValue<?>> arguments,
		@Nonnull Properties properties,
		@Nonnull List<KDLNode> children
	) {
		this.type = type;
		this.name = name;
		this.arguments = Collections.unmodifiableList(arguments);
		this.properties = properties;
		this.children = Collections.unmodifiableList(children);
	}

	/**
	 * @return the type of the node
	 */
	@Nonnull
	public Optional<String> getType() {
		return type;
	}

	/**
	 * @return the name of the node
	 */
	@Nonnull
	public String getName() {
		return name;
	}

	/**
	 * @return the arguments of the node
	 */
	@Nonnull
	public List<KDLValue<?>> getArguments() {
		return arguments;
	}


	/**
	 * @return the properties of the node
	 */
	@Nonnull
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Retrieves a property from the node. If a property is defined multiple times, the last value is returned.
	 *
	 * @param name the name of the property to retrieve
	 * @param <T>  the type of the property's value
	 * @return an option containing the last value of the property if it has any
	 */
	@Nonnull
	public <T> Optional<KDLValue<T>> getProperty(String name) {
		return properties.getValue(name);
	}

	/**
	 * @return the children of the node
	 */
	@Nonnull
	public List<KDLNode> getChildren() {
		return children;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		var kdlNode = (KDLNode) o;
		return Objects.equals(type, kdlNode.type) && Objects.equals(name, kdlNode.name)
			   && Objects.equals(arguments, kdlNode.arguments) && Objects.equals(properties, kdlNode.properties)
			   && Objects.equals(children, kdlNode.children);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, name, arguments, properties, children);
	}

	@Override
	public String toString() {
		var builder = new StringBuilder("KDLNode[");
		type.ifPresent(type -> builder.append('(').append(type).append(')'));
		builder.append(name);
		arguments.forEach(argument -> builder.append(' ').append(argument));
		properties.forEach(property -> builder.append(' ').append(property.getName()).append('=').append(property.getValue()));
		if (!children.isEmpty()) {
			builder.append('[')
				.append(children.stream().map(KDLNode::toString).collect(Collectors.joining(", ")))
				.append(']');
		}
		return builder.toString();
	}

	@Nonnull
	private final Optional<String> type;
	@Nonnull
	private final String name;
	@Nonnull
	private final List<KDLValue<?>> arguments;
	@Nonnull
	private final Properties properties;
	@Nonnull
	private final List<KDLNode> children;

	/**
	 * @return a new node builder
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A builder {@link KDLNode}.
	 */
	public static final class Builder {
		/**
		 * Sets the name of the node.
		 *
		 * @param name the name of the node
		 * @return {@code this}
		 */
		@Nonnull
		public Builder name(@Nonnull String name) {
			this.name = name;
			return this;
		}

		/**
		 * Sets the type of the node.
		 *
		 * @param type the type of the node, or {@code null} if it has no type
		 * @return {@code this}
		 */
		@Nonnull
		public Builder type(@Nullable String type) {
			this.type = type;
			return this;
		}

		/**
		 * Adds an argument to the node.
		 *
		 * @param value the value of the argument
		 * @return {@code this}
		 */
		@Nonnull
		public Builder argument(@Nullable Object value) {
			arguments.add(KDLValue.from(value));
			return this;
		}

		/**
		 * Adds a typed argument to the node.
		 *
		 * @param type  the type of the argument
		 * @param value the value of the argument
		 * @return {@code this}
		 */
		@Nonnull
		public Builder argument(@Nonnull String type, @Nullable Object value) {
			arguments.add(KDLValue.from(Optional.of(type), value));
			return this;
		}

		/**
		 * Adds a property to the node.
		 *
		 * @param name  the name of the property
		 * @param value the value of the property
		 * @return {@code this}
		 */
		@Nonnull
		public Builder property(@Nonnull String name, @Nullable Object value) {
			properties.property(name, KDLValue.from(value));
			return this;
		}

		/**
		 * Adds a typed property to the node.
		 *
		 * @param name  the name of property
		 * @param type  the type of the property
		 * @param value the value of the property
		 * @return {@code this}
		 */
		@Nonnull
		public Builder property(@Nonnull String name, @Nonnull String type, @Nullable Object value) {
			properties.property(name, KDLValue.from(Optional.of(type), value));
			return this;
		}

		/**
		 * Adds a child to the node.
		 *
		 * @param node the child to add
		 * @return {@code this}
		 */
		@Nonnull
		public Builder child(@Nonnull KDLNode node) {
			children.add(node);
			return this;
		}

		/**
		 * Adds a child to the node.
		 *
		 * @param node a builder for the child to add
		 * @return {@code this}
		 */
		@Nonnull
		public Builder child(@Nonnull Builder node) {
			children.add(node.build());
			return this;
		}

		/**
		 * Adds children to the node.
		 *
		 * @param nodes the children to add
		 * @return {@code this}
		 */
		@Nonnull
		public Builder children(@Nonnull List<KDLNode> nodes) {
			children.addAll(nodes);
			return this;
		}

		/**
		 * Adds children to the node.
		 *
		 * @param nodes the children to add
		 * @return {@code this}
		 */
		@Nonnull
		public Builder children(@Nonnull KDLNode... nodes) {
			children.addAll(Arrays.asList(nodes));
			return this;
		}

		/**
		 * Adds children to the node.
		 *
		 * @param nodes the builders of the children to add
		 * @return {@code this}
		 */
		@Nonnull
		public Builder children(@Nonnull Builder... nodes) {
			for (var node : nodes) {
				children.add(node.build());
			}
			return this;
		}

		/**
		 * Builds the node.
		 *
		 * @return the built node
		 */
		@Nonnull
		public KDLNode build() {
			return new KDLNode(Optional.ofNullable(type), Objects.requireNonNull(name), arguments, properties.build(), children);
		}

		@Nullable
		private String type;
		@Nullable
		private String name;
		@Nonnull
		private final List<KDLValue<?>> arguments = new ArrayList<>();
		@Nonnull
		private final Properties.Builder properties = Properties.builder();
		@Nonnull
		private final List<KDLNode> children = new ArrayList<>();
	}
}
