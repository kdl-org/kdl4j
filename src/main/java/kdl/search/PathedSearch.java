package kdl.search;

import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.search.mutation.Mutation;
import kdl.search.predicates.NodePredicate;

import java.util.*;

/**
 * Searches for nodes where the path to a node matches a series of predicates. The first predicate provided is used
 * to filter nodes at the root, second to filter nodes that are children of the nodes at the root, and so on.
 */
public class PathedSearch implements Search {
    private final NavigableMap<Integer, NodePredicate> path;

    private PathedSearch(NavigableMap<Integer, NodePredicate> path) {
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KDLDocument filter(KDLDocument document, boolean trim) {
        return filter(document, 0, trim).orElse(KDLDocument.empty());
    }

    private Optional<KDLDocument> filter(KDLDocument document, int depth, boolean trim) {
        final NodePredicate predicate = path.get(depth);
        if (predicate == null) {
            return Optional.empty();
        }

        final Integer maxKey = path.floorKey(Integer.MAX_VALUE);
        final KDLDocument.Builder builder = KDLDocument.builder();
        for (KDLNode node : document.getNodes()) {
            if (predicate.test(node)) {
                if (depth == maxKey) {
                    if (trim) {
                        builder.addNode(node.toBuilder().setChild(Optional.empty()).build());
                    } else {
                        builder.addNode(node);
                    }
                } else {
                    final Optional<KDLDocument> newChild = node.getChild().flatMap(ch -> filter(ch, depth + 1, trim));
                    if (newChild.isPresent()) {
                        builder.addNode(node.toBuilder().setChild(newChild).build());
                    }
                }
            }
        }

        final KDLDocument returnDoc = builder.build();
        if (returnDoc.getNodes().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(returnDoc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KDLDocument list(KDLDocument document, boolean trim) {
        final ArrayList<KDLNode> nodes = new ArrayList<>();
        list(document, trim, 0, nodes);
        return KDLDocument.builder().addNodes(nodes).build();
    }

    private void list(KDLDocument document, boolean trim, int depth, List<KDLNode> nodes) {
        final NodePredicate predicate = path.get(depth);
        if (predicate == null) {
            return;
        }

        final Integer maxKey = path.floorKey(Integer.MAX_VALUE);
        for (KDLNode node : document.getNodes()) {
            if (predicate.test(node)) {
                if (depth == maxKey) {
                    if (trim) {
                        nodes.add(node.toBuilder().setChild(Optional.empty()).build());
                    } else {
                        nodes.add(node);
                    }
                } else {
                    node.getChild().ifPresent(ch -> list(ch, trim, depth + 1, nodes));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KDLDocument mutate(KDLDocument document, Mutation mutation) {
        return mutate(document, mutation, 0).orElse(KDLDocument.empty());
    }

    public Optional<KDLDocument> mutate(KDLDocument document, Mutation mutation, int depth) {
        final NodePredicate predicate = path.get(depth);
        if (predicate == null) {
            return Optional.of(document);
        }

        final Integer maxKey = path.floorKey(Integer.MAX_VALUE);
        final KDLDocument.Builder docBuilder = KDLDocument.builder();
        for (KDLNode node : document.getNodes()) {
            if (predicate.test(node)) {
                if (depth == maxKey) {
                    mutation.apply(node).ifPresent(docBuilder::addNode);
                } else {
                    final KDLNode.Builder nodeBuilder = node.toBuilder();
                    node.getChild().ifPresent(ch -> nodeBuilder.setChild(mutate(ch, mutation, depth + 1)));
                    docBuilder.addNode(nodeBuilder.build());
                }
            } else {
                docBuilder.addNode(node);
            }
        }

        final KDLDocument newDoc = docBuilder.build();
        if (newDoc.getNodes().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(newDoc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean anyMatch(KDLDocument document) {
        return anyMatch(document, 0);
    }

    private boolean anyMatch(KDLDocument document, int depth) {
        final NodePredicate predicate = path.get(depth);
        if (predicate == null) {
            return false;
        }

        final Integer maxKey = path.floorKey(Integer.MAX_VALUE);
        for (KDLNode node : document.getNodes()) {
            if (predicate.test(node)) {
                if (depth == maxKey) {
                    return true;
                } else if (node.getChild().map(ch -> anyMatch(ch, depth +1)).orElse(false)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final NavigableMap<Integer, NodePredicate> predicates = new TreeMap<>();
        private int depth = 0;

        public Builder addLevel(NodePredicate predicate) {
            predicates.put(depth, predicate);
            depth++;
            return this;
        }

        public PathedSearch build() {
            return new PathedSearch(predicates);
        }
    }
}
