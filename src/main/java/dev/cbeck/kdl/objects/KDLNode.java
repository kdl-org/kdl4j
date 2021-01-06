package dev.cbeck.kdl.objects;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class KDLNode implements KDLObject {
    private final KDLIdentifier identifier;
    private final List<KDLProperty> propsAndArgs;
    private final Optional<KDLDocument> child;

    public KDLNode(KDLIdentifier identifier, List<KDLProperty> propsAndArgs, Optional<KDLDocument> child) {
        this.identifier = Objects.requireNonNull(identifier);
        this.propsAndArgs = Collections.unmodifiableList(Objects.requireNonNull(propsAndArgs));
        this.child = Objects.requireNonNull(child);
    }

    public KDLIdentifier getIdentifier() {
        return identifier;
    }

    public List<KDLProperty> getPropsAndArgs() {
        return propsAndArgs;
    }

    public Optional<KDLDocument> getChild() {
        return child;
    }

    @Override
    public String toString() {
        return "KDLNode{" +
                "identifier=" + identifier +
                ", propsAndArgs=" + propsAndArgs +
                ", child=" + child +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KDLNode)) return false;
        KDLNode kdlNode = (KDLNode) o;
        return Objects.equals(identifier, kdlNode.identifier) && Objects.equals(propsAndArgs, kdlNode.propsAndArgs) && Objects.equals(child, kdlNode.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, propsAndArgs, child);
    }
}
