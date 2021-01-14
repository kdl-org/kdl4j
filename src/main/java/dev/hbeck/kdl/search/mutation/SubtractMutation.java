package dev.hbeck.kdl.search.mutation;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLProperty;
import dev.hbeck.kdl.objects.KDLValue;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SubtractMutation implements Mutation {
    private final List<Predicate<KDLValue>> argPredicates;
    private final List<Predicate<KDLProperty>> propPredicates;
    private final boolean emptyChild;
    private final boolean deleteChild;

    public SubtractMutation(List<Predicate<KDLValue>> argPredicates, List<Predicate<KDLProperty>> propPredicates, boolean emptyChild, boolean deleteChild) {
        if (emptyChild && deleteChild) {
            throw new IllegalArgumentException("Only one of emptyChild and deleteChild may be set.");
        }

        this.argPredicates = argPredicates;
        this.propPredicates = propPredicates;
        this.emptyChild = emptyChild;
        this.deleteChild = deleteChild;
    }

    @Override
    public Optional<KDLNode> apply(KDLNode node) {
        if (argPredicates.isEmpty() && propPredicates.isEmpty() && !emptyChild && !deleteChild) {
            return Optional.empty();
        }

        final KDLNode.Builder builder = node.toBuilder();
        for (KDLValue arg : node.getArgs()) {
            for (Predicate<KDLValue> argPredicate : argPredicates) {
                if (argPredicate.test(arg)) {
                    builder.addArg(arg);
                }
            }
        }

        for (String propKey : node.getProps().keySet()) {
            final KDLProperty property = new KDLProperty(propKey, node.getProps().get(propKey));
            boolean matchesAny = false;
            for (Predicate<KDLProperty> propPredicate : propPredicates) {
                matchesAny |= propPredicate.test(property);
            }

            if (!matchesAny) {
                builder.addProp(property);
            }
        }

        if (emptyChild) {
            builder.setChild(KDLDocument.empty());
        } else if (!deleteChild) {
            builder.setChild(node.getChild());
        }

        return Optional.of(builder.build());
    }
}
