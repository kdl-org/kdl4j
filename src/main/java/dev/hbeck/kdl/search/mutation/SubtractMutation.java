package dev.hbeck.kdl.search.mutation;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLValue;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SubtractMutation implements Mutation {
    private final List<Predicate<KDLValue>> args;
    private final List<Predicate<String>> props;
    private final Optional<KDLDocument> child;

    private SubtractMutation(List<Predicate<KDLValue>> args, List<Predicate<String>> props, Optional<KDLDocument> child) {
        this.args = args;
        this.props = props;
        this.child = child;
    }

    @Override
    public Optional<KDLNode> apply(KDLNode node) {
        if (args.isEmpty() && props.isEmpty() && !child.isPresent()) {
            return Optional.empty();
        }

        final KDLNode.Builder builder = node.toBuilder();
        for (Predicate<KDLValue> argPredicate : args) {

        }


        return Optional.empty();
    }
}
