package kdl.objects;

import kdl.print.PrintConfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

/**
 * A model object representing a KDL Document. The only data in a document is the list of nodes, which may be empty.
 */
public class KDLDocument implements KDLObject {
    private final List<KDLNode> nodes;

    public KDLDocument(List<KDLNode> nodes) {
        this.nodes = Collections.unmodifiableList(Objects.requireNonNull(nodes));
    }

    public List<KDLNode> getNodes() {
        return nodes;
    }

    @Override
    public void writeKDL(Writer writer, PrintConfig printConfig) throws IOException {
        writeKDLPretty(writer, printConfig);
    }

    /**
     * Writes a text representation of the document to the provided writer
     *
     * @param writer the writer to write to
     * @param printConfig configuration controlling how the document is written
     * @throws IOException if there's any error writing the document
     */
    public void writeKDLPretty(Writer writer, PrintConfig printConfig)  throws IOException {
        writeKDL(writer, 0, printConfig);
    }

    /**
     * Write a text representation of the document to the provided writer with default 'pretty' settings
     *
     * @param writer the writer to write to
     * @throws IOException if there's any error writing the document
     */
    public void writeKDLPretty(Writer writer)  throws IOException {
        writeKDLPretty(writer, PrintConfig.PRETTY_DEFAULT);
    }

    /**
     * Get a string representation of the document
     *
     * @param printConfig configuration controlling how the document is written
     * @return the string
     */
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

    /**
     * Get a string representation of the document with default 'pretty' settings
     */
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

    /**
     * Get a document with no nodes
     *
     * @return the empty document
     */
    public static KDLDocument empty() {
        return new KDLDocument(new ArrayList<>());
    }

    /**
     * Get a builder used to gradually build a document
     *
     * @return the builder
     */
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
