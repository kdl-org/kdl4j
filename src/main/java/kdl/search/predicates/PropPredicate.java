package kdl.search.predicates;

import java.util.function.Predicate;
import kdl.objects.KDLNode;
import kdl.objects.KDLValue;

/**
 * Predicate matching a KDLNode property
 */
public class PropPredicate implements NodeContentPredicate {
    private final Predicate<String> keyPredicate;
    private final Predicate<KDLValue<?>> valuePredicate;

    public PropPredicate(Predicate<String> keyPredicate, Predicate<KDLValue<?>> valuePredicate) {
        this.keyPredicate = keyPredicate;
        this.valuePredicate = valuePredicate;
    }

    @Override
    public boolean test(KDLNode node) {
        for (String key : node.getProps().keySet()) {
            if (keyPredicate.test(key) && valuePredicate.test(node.getProps().get(key))) {
                return true;
            }
        }

        return false;
    }

    public Predicate<String> getKeyPredicate() {
        return keyPredicate;
    }

    public Predicate<KDLValue<?>> getValuePredicate() {
        return valuePredicate;
    }
}
