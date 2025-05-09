package dev.kdl;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A KDL document.
 *
 * @param nodes the nodes in the document
 */
public record KdlDocument(@Nonnull List<KdlNode> nodes) {
	/**
	 * Creates a new document with the provided nodes.
	 *
	 * @param nodes the nodes in the document
	 */
	public KdlDocument(@Nonnull List<KdlNode> nodes) {
		this.nodes = Collections.unmodifiableList(nodes);
	}

	/**
	 * Creates a new builder to create a new document from the current one.
	 *
	 * @return a new builder with the nodes of this document
	 */
	@Nonnull
	public Builder mutate() {
		return new Builder(nodes);
	}

	/**
	 * @return a new document builder
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A {@link KdlDocument} builder.
	 */
	public static final class Builder {

		private Builder() {
			this.nodes = new ArrayList<>();
		}

		private Builder(List<KdlNode> nodes) {
			this.nodes = new ArrayList<>(nodes);
		}

		/**
		 * Adds a node to the document being built.
		 *
		 * @param node a node to add
		 * @return this builder
		 */
		@Nonnull
		public Builder node(@Nonnull KdlNode node) {
			nodes.add(node);
			return this;
		}

		/**
		 * Adds a node to the document being built using a node builder.
		 *
		 * @param node a node builder to add
		 * @return this builder
		 */
		@Nonnull
		public Builder node(@Nonnull KdlNode.Builder node) {
			nodes.add(node.build());
			return this;
		}

		/**
		 * Adds nodes to the document being built.
		 *
		 * @param nodes nodes to add
		 * @return this builder
		 */
		@Nonnull
		public Builder nodes(@Nonnull KdlNode... nodes) {
			Collections.addAll(this.nodes, nodes);
			return this;
		}

		/**
		 * Adds nodes to the document being built using node builders.
		 *
		 * @param nodes node builders to add
		 * @return this builder
		 */
		@Nonnull
		public Builder nodes(@Nonnull KdlNode.Builder... nodes) {
			for (var node : nodes) {
				this.nodes.add(node.build());
			}
			return this;
		}

		/**
		 * Creates a new document.
		 *
		 * @return a new KDL document
		 */
		@Nonnull
		public KdlDocument build() {
			return new KdlDocument(nodes);
		}

		@Nonnull
		private final List<KdlNode> nodes;
	}
}
