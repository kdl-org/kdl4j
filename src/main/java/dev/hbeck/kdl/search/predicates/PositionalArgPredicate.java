package dev.hbeck.kdl.search.predicates;

import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLValue;

import java.util.function.Predicate;

public class PositionalArgPredicate implements NodeContentPredicate {
    private final int position;
    private final Predicate<KDLValue> predicate;

    public PositionalArgPredicate(int position, Predicate<KDLValue> predicate) {
        this.position = position;
        this.predicate = predicate;
    }

    @Override
    public boolean test(KDLNode node) {
        if (position >= node.getArgs().size()) {
            return predicate.test(node.getArgs().get(position));
        } else {
            return false;
        }
    }
}
