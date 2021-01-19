package dev.hbeck.kdl.search.predicates;

import dev.hbeck.kdl.objects.KDLNode;

/**
 * Returns true if a node has any contents, false otherwise
 */
public class AnyContentPredicate implements NodeContentPredicate {
    @Override
    public boolean test(KDLNode node) {
        return !node.getArgs().isEmpty()
                || !node.getProps().isEmpty()
                || (node.getChild().isPresent() && !node.getChild().get().getNodes().isEmpty());
    }
}
