package dev.hbeck.kdl.search.mutation;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class SetMutation implements Mutation {
    private final Optional<String> identifier;
    private final List<KDLValue> args;
    private final LinkedHashMap<Predicate<String>, KDLValue> props;
    private final Optional<Optional<KDLDocument>> child;

    private SetMutation(Optional<String> identifier, List<KDLValue> args, LinkedHashMap<Predicate<String>, KDLValue> props, Optional<Optional<KDLDocument>> child) {
        this.identifier = identifier;
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
            builder.addAllArgs(args);
        }

        for (Map.Entry<Predicate<String>, KDLValue> set : props.entrySet()) {
            for (String propKey : node.getProps().keySet()) {
                if (set.getKey().test(propKey)) {
                    builder.addProp(propKey, set.getValue());
                }
            }
        }

        return Optional.of(builder.build());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<KDLValue> args = new ArrayList<>();
        private final LinkedHashMap<Predicate<String>, KDLValue> props = new LinkedHashMap<>();
        private Optional<String> identifier = Optional.empty();
        private Optional<Optional<KDLDocument>> child = Optional.empty();

        public Builder addArg(KDLValue arg) {
            args.add(arg);
            return this;
        }

        public Builder addProp(Predicate<String> key, KDLValue value) {
            props.put(key, value);
            return this;
        }

        public Builder setChild(Optional<KDLDocument> child) {
            this.child = Optional.of(child);
            return this;
        }

        public Builder setIdentifier(Optional<String> identifier) {
            this.identifier = identifier;
            return this;
        }

        public SetMutation build() {
            return new SetMutation(identifier, args, props, child);
        }
    }
}
