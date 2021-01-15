package dev.hbeck.kdl.search.mutation;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AddMutation implements Mutation {
    private final List<KDLValue> args;
    private final Map<String, KDLValue> props;
    private final Optional<KDLDocument> child;

    public AddMutation(List<KDLValue> args, Map<String, KDLValue> props, Optional<KDLDocument> child) {
        this.args = args;
        this.props = props;
        this.child = child;
    }

    @Override
    public Optional<KDLNode> apply(KDLNode node) {
        final KDLNode.Builder builder = node.toBuilder();

        builder.addAllArgs(args);
        for (String key : props.keySet()) {
            builder.addProp(key, props.get(key));
        }

        if (node.getChild().isPresent() && child.isPresent()) {
            final KDLDocument newChild = node.getChild().get().toBuilder().addNodes(child.get().getNodes()).build();
            builder.setChild(newChild);
        } else {
            builder.setChild(child);
        }

        return Optional.of(builder.build());
    }
}
