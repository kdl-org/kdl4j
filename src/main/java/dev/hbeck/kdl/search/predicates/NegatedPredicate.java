package dev.hbeck.kdl.search.predicates;

import dev.hbeck.kdl.objects.KDLNode;

/**
 * Inverts the test of the wrapped node content predicate
 */
public class NegatedPredicate implements NodeContentPredicate {
    private final NodeContentPredicate predicate;

    public NegatedPredicate(NodeContentPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(KDLNode node) {
        return !predicate.test(node);
    }
}
