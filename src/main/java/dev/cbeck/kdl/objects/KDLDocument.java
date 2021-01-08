package dev.cbeck.kdl.objects;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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
    public void writeKDL(Writer writer) throws IOException {
        writeKDL(writer, 0, 0);
    }

    public void writeKDLPretty(Writer writer, int indent)  throws IOException {
        writeKDL(writer, indent, 0);
    }

    public String toKDLPretty(int indent) {
        final StringWriter writer = new StringWriter();

        try {
            writeKDLPretty(writer, indent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    void writeKDL(Writer writer, int indent, int depth) throws IOException {
        for (KDLNode node : nodes) {
            for (int i = 0; i < indent * depth; i++) {
                writer.write(' ');
            }
            node.writeKDLPretty(writer, indent, depth);
            writer.write('\n');
        }
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
