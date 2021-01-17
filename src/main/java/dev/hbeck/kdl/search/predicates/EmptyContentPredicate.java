package dev.hbeck.kdl.search.predicates;

import dev.hbeck.kdl.objects.KDLNode;

public class EmptyContentPredicate implements NodeContentPredicate {
    @Override
    public boolean test(KDLNode node) {
        return node.getArgs().isEmpty() && node.getProps().isEmpty() && !node.getChild().isPresent();
    }
}
