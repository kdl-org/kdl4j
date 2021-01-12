package dev.hbeck.kdl.search.predicates;

import dev.hbeck.kdl.objects.KDLNode;

public class ArgPredicate implements NodeContentPredicate {
    @Override
    public boolean test(KDLNode node) {
        return false;
    }
}
