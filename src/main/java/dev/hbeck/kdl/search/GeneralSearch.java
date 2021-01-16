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
import java.util.function.Predicate;

public class GeneralSearch implements Search<Mutation> {
    private final NodePredicate predicate;

    private int minDepth = 0;
    private int maxDepth = Integer.MAX_VALUE;

    public GeneralSearch(NodePredicate predicate) {
        this.predicate = predicate;
    }

    public GeneralSearch setMinDepth(int minDepth) {
        this.minDepth = minDepth;
        return this;
    }

    public GeneralSearch setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public List<KDLNode> listAll(KDLDocument document, boolean trim) {
        return Collections.unmodifiableList(listAll(document, trim, 0, new ArrayList<>()));
    }

    private List<KDLNode> listAll(KDLDocument doc, boolean trim, int depth, List<KDLNode> nodes) {
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

        return nodes;
    }

    public KDLDocument filter(KDLDocument document) {
        return filter(document, 0).orElse(KDLDocument.empty());
    }

    private Optional<KDLDocument> filter(KDLDocument doc, int depth) {
        if (depth > maxDepth) {
            return Optional.empty();
        }

        final KDLDocument.Builder builder = KDLDocument.builder();
        for (KDLNode node : doc.getNodes()) {
            final Optional<KDLDocument> newChild = node.getChild().flatMap(ch -> filter(ch, depth + 1));
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
            if (depth >= minDepth && nodeMatches(node)) {
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

    public static <T> Predicate<T> any() {
        return v -> true;
    }

    public static GeneralSearch of(KDLDocument document) {
        return new GeneralSearch(document);
    }
}
