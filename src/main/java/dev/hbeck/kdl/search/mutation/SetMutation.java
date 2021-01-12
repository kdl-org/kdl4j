package dev.hbeck.kdl.search.mutation;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SetMutation implements Mutation {
    private final List<KDLValue> args;
    private final Map<String, KDLValue> props;
    private final Optional<KDLDocument> child;

    private SetMutation(List<KDLValue> args, Map<String, KDLValue> props, Optional<KDLDocument> child) {
        this.args = args;
        this.props = props;
        this.child = child;
    }

    @Override
    public Optional<KDLNode> apply(KDLNode node) {

        return Optional.empty();
    }
}
