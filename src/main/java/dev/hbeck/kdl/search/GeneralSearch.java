package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.search.mutation.Mutation;
import dev.hbeck.kdl.search.predicates.NodePredicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class GeneralSearch implements Search {
    private final NodePredicate predicate;
    private final int minDepth;
    private final int maxDepth;

    public GeneralSearch(NodePredicate predicate, int minDepth, int maxDepth) {
        this.predicate = predicate;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
    }

    public List<KDLNode> listAll(KDLDocument document, boolean trim) {
        final ArrayList<KDLNode> nodes = new ArrayList<>();
        listAll(document, trim, 0, nodes);
        return Collections.unmodifiableList(nodes);
    }

    private void listAll(KDLDocument doc, boolean trim, int depth, List<KDLNode> nodes) {
        if (depth <= maxDepth) {
            for (KDLNode node : doc.getNodes()) {
                if (minDepth <= depth && predicate.test(node)) {
                    final KDLNode.Builder nodeBuilder = node.toBuilder();
                    if (trim) {
                        nodeBuilder.setChild(Optional.empty());
                    }

                    nodes.add(nodeBuilder.build());
                }

                node.getChild().ifPresent(ch -> listAll(ch, trim, depth + 1, nodes));
            }
        }
    }

    public KDLDocument filter(KDLDocument document) {
        return filter(document, 0).orElse(KDLDocument.empty());
    }

    private Optional<KDLDocument> filter(KDLDocument document, int depth) {
        if (depth > maxDepth) {
            return Optional.empty();
        }

        final KDLDocument.Builder builder = KDLDocument.builder();
        for (KDLNode node : document.getNodes()) {
            final Optional<KDLDocument> newChild = node.getChild().flatMap(doc -> filter(doc, depth + 1));
            if (newChild.isPresent()) {
                builder.addNode(node.toBuilder().setChild(newChild).build());
            } else if (predicate.test(node)) {
                builder.addNode(node.toBuilder().setChild(Optional.empty()).build());
            }
        }

        final KDLDocument returnDoc = builder.build();
        if (returnDoc.getNodes().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(returnDoc);
        }
    }

    public KDLDocument mutate(KDLDocument document, Mutation fun) {
        return mutate(fun, document, 0).orElse(KDLDocument.empty());
    }

    private Optional<KDLDocument> mutate(Function<KDLNode, Optional<KDLNode>> fun, KDLDocument doc, int depth) {
        if (depth > maxDepth) {
            return Optional.of(doc);
        }

        final KDLDocument.Builder docBuilder = KDLDocument.builder();
        for (KDLNode node : doc.getNodes()) {
            if (depth >= minDepth && predicate.test(node)) {
                if (node.getChild().isPresent()) {
                    final Optional<KDLDocument> newChild = node.getChild().flatMap(ch -> mutate(fun, ch, depth + 1));
                    final KDLNode newNode = node.toBuilder().setChild(newChild).build();
                    fun.apply(newNode).ifPresent(docBuilder::addNode);
                } else {
                    fun.apply(node).ifPresent(docBuilder::addNode);
                }
            } else {
                final Optional<KDLDocument> newChild = node.getChild().flatMap(ch -> mutate(fun, ch, depth + 1));
                docBuilder.addNode(node.toBuilder().setChild(newChild).build());
            }
        }

        final KDLDocument newDoc = docBuilder.build();
        if (newDoc.getNodes().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(newDoc);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private NodePredicate predicate = null;
        private int minDepth = 0;
        private int maxDepth = Integer.MAX_VALUE;

        public Builder setPredicate(NodePredicate predicate) {
            this.predicate = predicate;
            return this;
        }

        public Builder setMinDepth(int minDepth) {
            this.minDepth = minDepth;
            return this;
        }

        public Builder setMaxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        public GeneralSearch build() {
            if (minDepth < 0 || maxDepth < 0) {
                throw new IllegalArgumentException("Min depth and max depth must be greater than 0");
            }

            return new GeneralSearch(predicate, minDepth, maxDepth);
        }
    }
}
