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
		@Nonnull
		public Builder node(@Nonnull KdlNode node) {
			nodes.add(node);
			return this;
		}

		@Nonnull
		public Builder node(@Nonnull KdlNode.Builder node) {
			nodes.add(node.build());
			return this;
		}

		@Nonnull
		public Builder nodes(@Nonnull KdlNode... nodes) {
			Collections.addAll(this.nodes, nodes);
			return this;
		}

		@Nonnull
		public Builder nodes(@Nonnull KdlNode.Builder... nodes) {
			for (var node : nodes) {
				this.nodes.add(node.build());
			}
			return this;
		}

		@Nonnull
		public KdlDocument build() {
			return new KdlDocument(nodes);
		}

		@Nonnull
		private final List<KdlNode> nodes = new ArrayList<>();
	}
}
