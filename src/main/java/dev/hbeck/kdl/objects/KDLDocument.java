package dev.hbeck.kdl.objects;

import dev.hbeck.kdl.print.PrintConfig;
import dev.hbeck.kdl.search.predicates.NodePredicate;
import dev.hbeck.kdl.search.Operation;
import dev.hbeck.kdl.search.Search;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
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

    public Search search() {
        return Search.of(this);
    }

    public KDLDocument apply(Operation operation) {
        return apply(operation, 0);
    }

    private KDLDocument apply(Operation operation, int depth) {
        final Builder builder = KDLDocument.builder();

        final NodePredicate nodePredicate = operation.getPath().get(depth);
        for (KDLNode node : nodes) {
            if (nodePredicate.test(node)) {
                if (depth == operation.getDepth()) {
                    if (operation.getMutation().isPresent()) {
                        operation.getMutation().get().apply(node).ifPresent(builder::addNode);
                    } else {
                        builder.addNode(node);
                    }
                } else if (node.getChild().isPresent()) {
                    final KDLNode newNode = node.toBuilder().setChild(node.getChild().get().apply(operation, depth + 1)).build();
                    builder.addNode(newNode);
                }
            }
        }

        return builder.build();
    }

    @Override
    public void writeKDL(Writer writer, PrintConfig printConfig) throws IOException {
        writeKDL(writer, 0, PrintConfig.RAW_DEFAULT);
    }

    public void writeKDLPretty(Writer writer, PrintConfig printConfig)  throws IOException {
        writeKDL(writer, 0, printConfig);
    }
    public void writeKDLPretty(Writer writer)  throws IOException {
        writeKDLPretty(writer, PrintConfig.PRETTY_DEFAULT);
    }

    public String toKDLPretty(PrintConfig printConfig) {
        final StringWriter writer = new StringWriter();
        final BufferedWriter bufferedWriter = new BufferedWriter(writer);

        try {
            writeKDLPretty(bufferedWriter, printConfig);
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    public String toKDLPretty() {
        return toKDLPretty(PrintConfig.PRETTY_DEFAULT);
    }

    void writeKDL(Writer writer,int depth, PrintConfig printConfig) throws IOException {
        if (nodes.isEmpty() && depth == 0) {
            writer.write(printConfig.getNewline());
            return;
        }

        for (KDLNode node : nodes) {
            for (int i = 0; i < printConfig.getIndent() * depth; i++) {
                writer.write(printConfig.getIndentChar());
            }
            node.writeKDLPretty(writer, depth, printConfig);
            if (printConfig.shouldRequireSemicolons()) {
                writer.write(';');
            }
            writer.write(printConfig.getNewline());
        }
    }

    public Builder toBuilder() {
        return KDLDocument.builder()
                .addNodes(nodes);
    }

    public static KDLDocument empty() {
        return new KDLDocument(new ArrayList<>());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        final List<KDLNode> nodes = new ArrayList<>();

        public Builder addNode(KDLNode node) {
            nodes.add(node);
            return this;
        }

        public Builder addNodes(Collection<KDLNode> nodeCollection) {
            nodes.addAll(nodeCollection);
            return this;
        }

        public KDLDocument build() {
            return new KDLDocument(new ArrayList<>(nodes));
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
