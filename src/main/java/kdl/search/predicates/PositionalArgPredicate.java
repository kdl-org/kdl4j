package kdl.search.predicates;

import kdl.objects.KDLNode;
import kdl.objects.KDLValue;

import java.util.function.Predicate;

/**
 * Predicate matching a node based on a specific argument to a node based on that arguments position in the argument list.
 */
public class PositionalArgPredicate implements NodeContentPredicate {
    private final int position;
    private final Predicate<KDLValue> predicate;

    public PositionalArgPredicate(int position, Predicate<KDLValue> predicate) {
        this.position = position;
        this.predicate = predicate;
    }

    @Override
    public boolean test(KDLNode node) {
        if (position < node.getArgs().size()) {
            return predicate.test(node.getArgs().get(position));
        } else {
            return false;
        }
    }
}
