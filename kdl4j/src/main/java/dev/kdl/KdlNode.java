package dev.kdl;

import dev.kdl.parse.context.ParseContext;
import dev.kdl.parse.context.Span;
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
 * @param type         the type of the node
 * @param name         the name of the node
 * @param arguments    the arguments of the node
 * @param properties   the properties of the node
 * @param children     the children of the node
 * @param parseContext the context in which the node was parsed, if any
 * @param nameSpan     the span of the node name, if any
 * @param typeSpan     the span of the node type annotation, if any
 */
public record KdlNode(
	@Nullable String type,
	@Nonnull String name,
	@Nonnull List<KdlValue<?>> arguments,
	@Nonnull KdlProperties properties,
	@Nonnull List<KdlNode> children,
	@Nullable ParseContext parseContext,
	@Nullable Span nameSpan,
	@Nullable Span typeSpan
) {
	public KdlNode {
		arguments = Collections.unmodifiableList(arguments);
		children = Collections.unmodifiableList(children);
	}

	/**
	 * Creates a new KDL node without any location information.
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
		this(type, name, arguments, properties, children, null, null, null);
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

	@Nonnull
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof KdlNode that)) {
			return false;
		}
		return Objects.equals(type, that.type)
			&& name.equals(that.name)
			&& arguments.equals(that.arguments)
			&& properties.equals(that.properties)
			&& children.equals(that.children);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, name, arguments, properties, children);
	}

	/**
	 * Creates a new builder to create a new node from the current one.
	 *
	 * @return a new builder with the type, name, arguments, properties, and children of this document
	 */
	@Nonnull
	public Builder mutate() {
		return new Builder(type, name, arguments, properties, children, parseContext, nameSpan, typeSpan);
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
		private Builder() {
			this.arguments = new ArrayList<>();
			this.properties = KdlProperties.builder();
			this.children = new ArrayList<>();
		}

		private Builder(
			@Nullable String type,
			@Nullable String name,
			@Nonnull List<KdlValue<?>> arguments,
			@Nonnull KdlProperties properties,
			@Nonnull List<KdlNode> children,
			@Nullable ParseContext parseContext,
			@Nullable Span nameSpan,
			@Nullable Span typeSpan
		) {
			this.type = type;
			this.name = name;
			this.arguments = new ArrayList<>(arguments);
			this.properties = properties.mutate();
			this.children = new ArrayList<>(children);
			this.parseContext = parseContext;
			this.nameSpan = nameSpan;
			this.typeSpan = typeSpan;
		}

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
		 * Sets the parse context of the node.
		 *
		 * @param parseContext the parse context of the node, or {@code null} if it has none
		 * @return {@code this}
		 */
		@Nonnull
		public Builder parseContext(@Nullable ParseContext parseContext) {
			this.parseContext = parseContext;
			return this;
		}

		/**
		 * Sets the span of the name of the node.
		 *
		 * @param nameSpan the span of the name of the node, or {@code null} if it has none
		 * @return {@code this}
		 */
		@Nonnull
		public Builder nameSpan(@Nullable Span nameSpan) {
			this.nameSpan = nameSpan;
			return this;
		}

		/**
		 * Sets the span of the type annotation of the node.
		 *
		 * @param typeSpan the span of the type annotation of the node, or {@code null} if it has none
		 * @return {@code this}
		 */
		@Nonnull
		public Builder typeSpan(@Nullable Span typeSpan) {
			this.typeSpan = typeSpan;
			return this;
		}

		/**
		 * Builds the node.
		 *
		 * @return the built node
		 */
		@Nonnull
		public KdlNode build() {
			return new KdlNode(type, Objects.requireNonNull(name), arguments, properties.build(), children, parseContext, nameSpan, typeSpan);
		}

		@Nullable
		private String type;
		@Nullable
		private String name;
		@Nonnull
		private final ArrayList<KdlValue<?>> arguments;
		@Nonnull
		private final KdlProperties.Builder properties;
		@Nonnull
		private final ArrayList<KdlNode> children;
		@Nullable
		private ParseContext parseContext;
		@Nullable
		private Span nameSpan;
		@Nullable
		private Span typeSpan;
	}
}
