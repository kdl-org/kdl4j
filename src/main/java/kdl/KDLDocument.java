package kdl;

import jakarta.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A KDL document.
 */
public class KDLDocument {
	/**
	 * Creates a new document
	 *
	 * @param nodes the nodes in the document
	 */
	public KDLDocument(@Nonnull List<KDLNode> nodes) {
		this.nodes = Collections.unmodifiableList(nodes);
	}

	/**
	 * The nodes of the document.
	 *
	 * @return an immutable list containing the nodes of the document
	 */
	@Nonnull
	public List<KDLNode> getNodes() {
		return nodes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		var that = (KDLDocument) o;
		return Objects.equals(nodes, that.nodes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nodes);
	}

	@Override
	public String toString() {
		return "KDLDocument[" + nodes.stream().map(KDLNode::toString).collect(Collectors.joining(", ")) + ']';
	}

	@Nonnull
	private final List<KDLNode> nodes;
}
