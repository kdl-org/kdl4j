package dev.hbeck.kdl.search.predicates;

import dev.hbeck.kdl.objects.KDLNode;

public class DisjunctionPredicate implements NodeContentPredicate {
    private final NodeContentPredicate predOne;
    private final NodeContentPredicate predTwo;

    public DisjunctionPredicate(NodeContentPredicate predOne, NodeContentPredicate predTwo) {
        this.predOne = predOne;
        this.predTwo = predTwo;
    }

    @Override
    public boolean test(KDLNode node) {
        return predOne.test(node) || predTwo.test(node);
    }
}
