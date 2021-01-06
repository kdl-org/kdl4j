package dev.cbeck.kdl.objects;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class KDLDocument implements KDLObject {
    private final List<KDLNode> nodes;

    public KDLDocument(List<KDLNode> nodes) {
        this.nodes = Collections.unmodifiableList(Objects.requireNonNull(nodes));
    }

    public List<KDLNode> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "KDLDocument{" +
                "nodes=" + nodes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KDLDocument)) return false;
        KDLDocument that = (KDLDocument) o;
        return Objects.equals(nodes, that.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes);
    }
}
