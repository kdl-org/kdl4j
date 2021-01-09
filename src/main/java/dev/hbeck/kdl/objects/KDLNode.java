package dev.hbeck.kdl.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class KDLNode implements KDLObject {
    private final String identifier;
    private final Map<String, KDLValue> props;
    private final List<KDLValue> args;
    private final Optional<KDLDocument> child;

    public KDLNode(String identifier, Map<String, KDLValue> props, List<KDLValue> args, Optional<KDLDocument> child) {
        this.identifier = Objects.requireNonNull(identifier);
        this.props = Collections.unmodifiableMap(Objects.requireNonNull(props));
        this.args = Collections.unmodifiableList(args);
        this.child = Objects.requireNonNull(child);
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, KDLValue> getProps() {
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
        PrintUtil.writeStringQuotedAppropriately(writer, identifier, true);
        if (!args.isEmpty() || !props.isEmpty() || child.isPresent()) {
            writer.write(' ');
        }

        for (int i = 0; i < args.size(); i++) {
            args.get(i).writeKDL(writer);
            if (i < args.size() - 1 || !props.isEmpty() || child.isPresent()) {
                writer.write(' ');
            }
        }

        final ArrayList<String> keys = new ArrayList<>(props.keySet());
        for (int i = 0; i < keys.size(); i++) {
            PrintUtil.writeStringQuotedAppropriately(writer, keys.get(i), true);
            writer.write('=');
            props.get(keys.get(i)).writeKDL(writer);
            if (i < keys.size() - 1 || child.isPresent()) {
                writer.write(' ');
            }
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
