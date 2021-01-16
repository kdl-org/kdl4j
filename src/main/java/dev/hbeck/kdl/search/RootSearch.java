package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.search.mutation.AddMutation;
import dev.hbeck.kdl.search.mutation.Mutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RootSearch implements Search {
    private static final KDLNode EMPTY_NODE = KDLNode.builder().setIdentifier("empty").build();

    @Override
    public KDLDocument filter(KDLDocument document) {
        return document;
    }

    @Override
    public List<KDLNode> listAll(KDLDocument document, boolean trim) {
        return Collections.unmodifiableList(listAll(document, trim, new ArrayList<>()));
    }

    private List<KDLNode> listAll(KDLDocument document, boolean trim, List<KDLNode> nodes) {
        for (KDLNode node : document.getNodes()) {
            final KDLNode.Builder nodeBuilder = node.toBuilder();
            if (trim) {
                nodeBuilder.setChild(Optional.empty());
            }

            nodes.add(nodeBuilder.build());
            nodes.addAll(node.getChild().map(doc -> listAll(doc, trim)).orElse(Collections.emptyList()));
        }

        return nodes;
    }

    @Override
    public KDLDocument mutate(KDLDocument document, Mutation mutation) {
        if (!(mutation instanceof AddMutation)) {
            throw new IllegalArgumentException("Only AddMutations are allowed in RootSearch.mutate()");
        }
        final AddMutation addMutation = (AddMutation) mutation;

        if (!addMutation.getArgs().isEmpty() || !addMutation.getProps().isEmpty()) {
            throw new IllegalArgumentException("AddMutation on the root can only contain child alterations");
        } else if (!addMutation.getChild().isPresent() || addMutation.getChild().get().getNodes().isEmpty()) {
            return document;
        } else {
            final KDLNode result = mutation.apply(EMPTY_NODE).orElse(EMPTY_NODE);
            return document.toBuilder()
                    .addNodes(result.getChild().orElse(KDLDocument.empty()).getNodes())
                    .build();
        }
    }
}
