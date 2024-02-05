package kdl.search.mutation;

import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.objects.KDLValue;

import java.util.*;

/**
 * Mutation setting various fields of a node.
 *  - If identifier is provided, will set the node's name to the provided value
 *  - If any non-positional args are provided the argument list of the node will be cleared and replaced by the provided
 *    arguments. Note that if positional arguments are provided the clear will occur prior to any arguments being set,
 *    then the positional arguments will be set, then any non-positional arguments appended to the right side.
 *  - If any (position, value) pairs are in positionalArgs, then the arguments at the specified index will be set to the
 *    provided value. If the current list is smaller than position, the argument list will be right-padded with 'null'
 *    values to allow the position to be set.
 *  - If any properties are specified in props, the key=value pair will be set on the node, or added if it wasn't present.
 *  - If child is provided, the current child will be discarded and replaced be the provided one.
 */
public class SetMutation implements Mutation {
    private final Optional<String> identifier;
    private final Map<Integer, KDLValue<?>> positionalArgs;
    private final List<KDLValue<?>> args;
    private final Map<String, KDLValue<?>> props;
    private final Optional<Optional<KDLDocument>> child;

    private SetMutation(Optional<String> identifier, Map<Integer, KDLValue<?>> positionalArgs, List<KDLValue<?>> args,
                        Map<String, KDLValue<?>> props, Optional<Optional<KDLDocument>> child) {

        this.identifier = identifier;
        this.positionalArgs = positionalArgs;
        this.args = args;
        this.props = props;
        this.child = child;
    }

    @Override
    public Optional<KDLNode> apply(KDLNode node) {
        final KDLNode.Builder builder = node.toBuilder();
        identifier.ifPresent(builder::setIdentifier);
        child.ifPresent(builder::setChild);

        if (!args.isEmpty()) {
            builder.clearArgs();
        }

        for (Map.Entry<Integer, KDLValue<?>> positionalArg : positionalArgs.entrySet()) {
            builder.insertArgAt(positionalArg.getKey(), positionalArg.getValue());
        }

        builder.addAllArgs(args);

        if (!props.isEmpty()) {
            builder.clearProps();
            builder.addAllProps(props);
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
        private Optional<String> identifier = Optional.empty();
        private Optional<Optional<KDLDocument>> child = Optional.empty();

        public Builder addArg(KDLValue<?> arg) {
            args.add(arg);
            return this;
        }

        public Builder addProp(String key, KDLValue<?> value) {
            props.put(key, value);
            return this;
        }

        public Builder setChild(Optional<KDLDocument> child) {
            this.child = Optional.of(child);
            return this;
        }

        public Builder setIdentifier(String identifier) {
            this.identifier = Optional.of(identifier);
            return this;
        }

        public Builder addPositionalArg(int position, KDLValue<?> arg) {
            this.positionalArgs.put(position, arg);
            return this;
        }

        public SetMutation build() {
            return new SetMutation(identifier, positionalArgs, args, props, child);
        }
    }
}
