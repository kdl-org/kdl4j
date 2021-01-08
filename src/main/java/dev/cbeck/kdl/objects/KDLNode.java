package dev.cbeck.kdl.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class KDLNode implements KDLObject {
    private final KDLIdentifier identifier;
    private final Map<KDLIdentifier, KDLValue> props;
    private final List<KDLValue> args;
    private final Optional<KDLDocument> child;

    public KDLNode(KDLIdentifier identifier, Map<KDLIdentifier, KDLValue> props, List<KDLValue> args, Optional<KDLDocument> child) {
        this.identifier = Objects.requireNonNull(identifier);
        this.props = Collections.unmodifiableMap(Objects.requireNonNull(props));
        this.args = Collections.unmodifiableList(args);
        this.child = Objects.requireNonNull(child);
    }

    public KDLIdentifier getIdentifier() {
        return identifier;
    }

    public Map<KDLIdentifier, KDLValue> getProps() {
        return props;
    }

    public List<KDLValue> getArgs() {
        return args;
    }

    public Optional<KDLDocument> getChild() {
        return child;
    }

    @Override
    public void writeKDL(Writer writer) throws IOException {
        writeKDLPretty(writer, 0, 0);
    }

    void writeKDLPretty(Writer writer, int indent, int depth) throws IOException {
        writer.write(identifier.getIdentifier());
        writer.write(' ');

        for (KDLValue arg : args) {
            writer.write(arg.toKDL());
            writer.write(' ');
        }

        for (Map.Entry<KDLIdentifier, KDLValue> entry : props.entrySet()) {
            writer.write(entry.getKey().toKDL());
            writer.write('=');
            writer.write(entry.getValue().toKDL());
            writer.write(' ');
        }

        if (child.isPresent()) {
            writer.write("{\n");
            child.get().writeKDL(writer, indent, depth + 1);
            for (int i = 0; i < indent * depth; i++) {
                writer.write(' ');
            }
            writer.write('}');
        }
    }

    @Override
    public String toString() {
        return "KDLNode{" +
                "identifier=" + identifier +
                ", props=" + props +
                ", args=" + args +
                ", child=" + child +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KDLNode)) return false;
        KDLNode kdlNode = (KDLNode) o;
        return Objects.equals(identifier, kdlNode.identifier) && Objects.equals(props, kdlNode.props) && Objects.equals(args, kdlNode.args) && Objects.equals(child, kdlNode.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, props, args, child);
    }
}
