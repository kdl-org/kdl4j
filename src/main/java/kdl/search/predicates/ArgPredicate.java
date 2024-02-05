package kdl.search.predicates;

import kdl.objects.KDLNode;
import kdl.objects.KDLValue;

import java.util.function.Predicate;

/**
 * Matches nodes based on applying a given predicate to all arguments, returning true if any match.
 */
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
