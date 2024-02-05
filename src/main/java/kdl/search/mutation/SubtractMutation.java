package kdl.search.mutation;

import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.objects.KDLProperty;
import kdl.objects.KDLValue;
import kdl.parse.KDLInternalException;

import java.util.*;
import java.util.function.Predicate;

/**
 * Mutation removing contents of a node or the node itself.
 *
 * Args are removed if their index is included in positionalArgs or are matched by an argPredicate
 * Properties are removed if they match a predicate in propPredicates
 * Only one of emptyChild and deleteChild may be set.
 *  - If set, emptyChild will remove all contents from the node's child, leaving an empty child
 *  - If set, deleteChild will remove the child entirely
 */
public class SubtractMutation implements Mutation {
    private final Set<Integer> positionalArgs;
    private final List<Predicate<KDLValue<?>>> argPredicates;
    private final List<Predicate<KDLProperty>> propPredicates;
    private final boolean emptyChild;
    private final boolean deleteChild;

    private SubtractMutation(Set<Integer> positionalArgs, List<Predicate<KDLValue<?>>> argPredicates,
                             List<Predicate<KDLProperty>> propPredicates, boolean emptyChild, boolean deleteChild) {
        if (emptyChild && deleteChild) {
            throw new IllegalArgumentException("Only one of emptyChild and deleteChild may be set.");
        }

        this.positionalArgs = positionalArgs;
        this.argPredicates = argPredicates;
        this.propPredicates = propPredicates;
        this.emptyChild = emptyChild;
        this.deleteChild = deleteChild;
    }

    @Override
    public Optional<KDLNode> apply(KDLNode node) {
        if (argPredicates.isEmpty() && propPredicates.isEmpty() && positionalArgs.isEmpty() && !emptyChild && !deleteChild) {
            return Optional.empty();
        }

        final KDLNode.Builder builder = KDLNode.builder().setIdentifier(node.getIdentifier());

        for (int i = 0; i < node.getArgs().size(); i++) {
            if (!positionalArgs.contains(i)) {
                boolean matchesAny = false;
                for (Predicate<KDLValue<?>> argPredicate : argPredicates) {
                    if (argPredicate.test(node.getArgs().get(i))) {
                        matchesAny = true;
                    }
                }

                if (!matchesAny) {
                    builder.addArg(node.getArgs().get(i));
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

        if (emptyChild && node.getChild().isPresent()) {
            builder.setChild(KDLDocument.empty());
        } else if (!deleteChild) {
            builder.setChild(node.getChild());
        }

        return Optional.of(builder.build());
    }

    public static SubtractMutation deleteNodeMutation() {
        return new SubtractMutation(Collections.emptySet(), Collections.emptyList(), Collections.emptyList(), false, false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Predicate<KDLValue<?>>> argPredicates = new ArrayList<>();
        private final List<Predicate<KDLProperty>> propPredicates = new ArrayList<>();
        private final Set<Integer> positionalArgs = new HashSet<>();
        private boolean emptyChild = false;
        private boolean deleteChild = false;

        public Builder addArg(Predicate<KDLValue<?>> predicate) {
            argPredicates.add(predicate);
            return this;
        }

        public Builder addProp(Predicate<KDLProperty> predicate) {
            propPredicates.add(predicate);
            return this;
        }

        public Builder deleteChild() {
            this.deleteChild = true;
            return this;
        }

        public Builder emptyChild() {
            this.emptyChild = true;
            return this;
        }

        public Builder deleteArgAt(int position) {
            this.positionalArgs.add(position);
            return this;
        }

        public SubtractMutation build() {
            if (emptyChild && deleteChild) {
                throw new KDLInternalException("Only one of empty child and delete child may be specified");
            }

            return new SubtractMutation(positionalArgs, argPredicates, propPredicates, emptyChild, deleteChild);
        }
    }
}
