package kdl.search.mutation;

import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.objects.KDLValue;

import java.util.*;

/**
 * Mutation adding fields to a node
 *  - If any positional args are specified, they will be inserted and other args shifted right to accommodate.
 *  - If any other args are specified, they'll be added to the right side of the argument list.
 *  - If any properties are specified, they'll be added to the properties of the map, overwriting any previous values
 *    if an existing key is specified.
 *  - If a child is specified, any nodes in the child will be appended to the existing child if one exists. If none exists,
 *    the child will be set to the provided child.
 */
public class AddMutation implements Mutation {
    private final List<KDLValue<?>> args;
    private final Map<Integer, KDLValue<?>> positionalArgs;
    private final Map<String, KDLValue<?>> props;
    private final Optional<KDLDocument> child;

    private AddMutation(List<KDLValue<?>> args, Map<Integer, KDLValue<?>> positionalArgs, Map<String, KDLValue<?>> props, Optional<KDLDocument> child) {
        this.args = args;
        this.positionalArgs = positionalArgs;
        this.props = props;
        this.child = child;
    }

    public Map<Integer, KDLValue<?>> getPositionalArgs() {
        return positionalArgs;
    }

    public List<KDLValue<?>> getArgs() {
        return args;
    }

    public Map<String, KDLValue<?>> getProps() {
        return props;
    }

    public Optional<KDLDocument> getChild() {
        return child;
    }

    @Override
    public Optional<KDLNode> apply(KDLNode node) {
        final KDLNode.Builder builder = node.toBuilder();

        for (Map.Entry<Integer, KDLValue<?>> positionalArg : positionalArgs.entrySet()) {
            builder.insertArgAt(positionalArg.getKey(), positionalArg.getValue());
        }

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<Integer, KDLValue<?>> positionalArgs = new HashMap<>();
        private final List<KDLValue<?>> args = new ArrayList<>();
        private final Map<String, KDLValue<?>> props = new HashMap<>();
        private Optional<KDLDocument> child = Optional.empty();

        public Builder addArg(KDLValue<?> arg) {
            args.add(arg);
            return this;
        }

        public Builder addProp(String key, KDLValue<?> value) {
            props.put(key, value);
            return this;
        }

        public Builder setChild(KDLDocument child) {
            this.child = Optional.of(child);
            return this;
        }

        public Builder addPositionalArg(int position, KDLValue<?> arg) {
            this.positionalArgs.put(position, arg);
            return this;
        }

        public AddMutation build() {
            return new AddMutation(args, positionalArgs, props, child);
        }
    }
}
