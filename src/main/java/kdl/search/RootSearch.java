package kdl.search;

import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.search.mutation.Mutation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A "Search" that operates purely on the root with no predicates. Primarily used for applying mutations to the root.
 */
public class RootSearch implements Search {
    private static final KDLNode EMPTY_NODE = KDLNode.builder().setIdentifier("empty").build();

    /**
     * {@inheritDoc}
     */
    @Override
    public KDLDocument filter(KDLDocument document, boolean trim) {
        if (trim) {
            final KDLDocument.Builder builder = KDLDocument.builder();
            for (KDLNode node : document.getNodes()) {
                builder.addNode(node.toBuilder().setChild(Optional.empty()).build());
            }
            return builder.build();
        } else {
            return document;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KDLDocument list(KDLDocument document, boolean trim) {
        final ArrayList<KDLNode> nodes = new ArrayList<>();
        list(document, trim, nodes);
        return KDLDocument.builder().addNodes(nodes).build();
    }

    private void list(KDLDocument document, boolean trim, List<KDLNode> nodes) {
        for (KDLNode node : document.getNodes()) {
            final KDLNode.Builder nodeBuilder = node.toBuilder();
            if (trim) {
                nodeBuilder.setChild(Optional.empty());
            }

            nodes.add(nodeBuilder.build());
            node.getChild().ifPresent(doc -> list(doc, trim, nodes));
        }
    }

    /**
     * Applies mutation to a temporary, empty node. If the resulting node is present and has a child, any nodes
     * contained in it are added to the root and the result returned. All other aspects of the returned node are ignored.
     */
    @Override
    public KDLDocument mutate(KDLDocument document, Mutation mutation) {
        final KDLNode result = mutation.apply(EMPTY_NODE).orElse(EMPTY_NODE);
        return document.toBuilder()
                .addNodes(result.getChild().orElse(KDLDocument.empty()).getNodes())
                .build();
    }

    @Override
    public boolean anyMatch(KDLDocument document) {
        return !document.getNodes().isEmpty();
    }
}
