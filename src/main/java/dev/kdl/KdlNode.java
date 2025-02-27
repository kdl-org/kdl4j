package dev.kdl;

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
 *
 * @param type       the type of the node
 * @param name       the name of the node
 * @param arguments  the arguments of the node
 * @param properties the properties of the node
 * @param children   the children of the node
 */
public record KdlNode(
	@Nullable String type,
	@Nonnull String name,
	@Nonnull List<KdlValue<?>> arguments,
	@Nonnull KdlProperties properties,
	@Nonnull List<KdlNode> children
) {
	/**
	 * Creates a new KDL node.
	 *
	 * @param type       the type of the node
	 * @param name       the name of the node
	 * @param arguments  the arguments of the node
	 * @param properties the properties of the node
	 * @param children   the children of the node
	 */
	public KdlNode(
		@Nullable String type,
		@Nonnull String name,
		@Nonnull List<KdlValue<?>> arguments,
		@Nonnull KdlProperties properties,
		@Nonnull List<KdlNode> children
	) {
		this.type = type;
		this.name = name;
		this.arguments = Collections.unmodifiableList(arguments);
		this.properties = properties;
		this.children = Collections.unmodifiableList(children);
	}

	/**
	 * Retrieves a property from the node. If a property is defined multiple times, the last value is returned.
	 *
	 * @param name the name of the property to retrieve
	 * @param <T>  the type of the property's value
	 * @return an option containing the last value of the property if it has any
	 */
	@Nonnull
	public <T> Optional<KdlValue<T>> getProperty(String name) {
		return properties.getValue(name);
	}

	@Override
	public String toString() {
		var builder = new StringBuilder("KDLNode(");
		if (type != null) {
			builder.append('(').append(type).append(')');
		}
		builder.append(name);
		arguments.forEach(argument -> builder.append(' ').append(argument));
		properties.forEach(property -> builder.append(' ').append(property));
		if (!children.isEmpty()) {
			builder.append('[')
				.append(children.stream().map(KdlNode::toString).collect(Collectors.joining(", ")))
				.append(']');
		}
		builder.append(')');
		return builder.toString();
	}

	/**
	 * @return a new node builder
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A {@link KdlNode} builder.
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
			arguments.add(KdlValue.from(value));
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
		public Builder argument(@Nullable String type, @Nullable Object value) {
			arguments.add(KdlValue.from(type, value));
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
			properties.property(name, KdlValue.from(value));
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
		public Builder property(@Nonnull String name, @Nullable String type, @Nullable Object value) {
			properties.property(name, KdlValue.from(type, value));
			return this;
		}

		/**
		 * Adds a child to the node.
		 *
		 * @param node the child to add
		 * @return {@code this}
		 */
		@Nonnull
		public Builder child(@Nonnull KdlNode node) {
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
		public Builder children(@Nonnull List<KdlNode> nodes) {
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
		public Builder children(@Nonnull KdlNode... nodes) {
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
		public KdlNode build() {
			return new KdlNode(type, Objects.requireNonNull(name), arguments, properties.build(), children);
		}

		@Nullable
		private String type;
		@Nullable
		private String name;
		@Nonnull
		private final List<KdlValue<?>> arguments = new ArrayList<>();
		@Nonnull
		private final KdlProperties.Builder properties = KdlProperties.builder();
		@Nonnull
		private final List<KdlNode> children = new ArrayList<>();
	}
}
