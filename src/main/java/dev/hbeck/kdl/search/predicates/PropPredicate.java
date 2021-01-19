package dev.hbeck.kdl.search.predicates;

import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLValue;

import java.util.function.Predicate;

/**
 * Predicate matching a KDLNode property
 */
public class PropPredicate implements NodeContentPredicate {
    private final Predicate<String> keyPredicate;
    private final Predicate<KDLValue> valuePredicate;

    public PropPredicate(Predicate<String> keyPredicate, Predicate<KDLValue> valuePredicate) {
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

    public Predicate<KDLValue> getValuePredicate() {
        return valuePredicate;
    }
}
