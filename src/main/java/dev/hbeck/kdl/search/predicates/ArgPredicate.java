package dev.hbeck.kdl.search.predicates;

import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLValue;

import java.util.function.Predicate;

public class ArgPredicate implements NodeContentPredicate {
    private final Predicate<KDLValue> predicate;

    public ArgPredicate(Predicate<KDLValue> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(KDLNode node) {
        for (KDLValue arg : node.getArgs()) {
            if (predicate.test(arg)) {
                return true;
            }
        }

        return false;
    }

    public Predicate<KDLValue> getPredicate() {
        return predicate;
    }
}
