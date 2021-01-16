package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.search.mutation.Mutation;
import dev.hbeck.kdl.search.predicates.NodePredicate;

import java.util.List;
import java.util.Map;

public class PathedSearch implements Search<Mutation>{
    private final Map<Integer, NodePredicate> path;

    @Override
    public KDLDocument filter(KDLDocument document) {
        return filter(document, 0);
    }

    private KDLDocument filter(KDLDocument document, int depth) {

    }

    @Override
    public List<KDLNode> listAll(KDLDocument document, boolean trim) {
        return null;
    }

    @Override
    public KDLDocument mutate(KDLDocument document, Mutation mutation) {
        return null;
    }
}
