package dev.hbeck.kdl.objects;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class KDLNode implements KDLObject {
    private final String identifier;
    private final Map<String, KDLValue> props;
    private final List<KDLValue> args;
    private final Optional<KDLDocument> child;

    public KDLNode(String identifier, Map<String, KDLValue> props, List<KDLValue> args, Optional<KDLDocument> child) {
        this.identifier = Objects.requireNonNull(identifier);
        this.props = Collections.unmodifiableMap(Objects.requireNonNull(props));
        this.args = Collections.unmodifiableList(Objects.requireNonNull(args));
        this.child = Objects.requireNonNull(child);
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, KDLValue> getProps() {
        return props;
    }

    public List<KDLValue> getArgs() {
        return args;
    }

    public Optional<KDLDocument> getChild() {
        return child;
    }

    @Override
    public void writeKDL(Writer writer) throws IOException {
        writeKDLPretty(writer, 0, 0);
    }

    void writeKDLPretty(Writer writer, int indent, int depth) throws IOException {
        PrintUtil.writeStringQuotedAppropriately(writer, identifier, true);
        if (!args.isEmpty() || !props.isEmpty() || child.isPresent()) {
            writer.write(' ');
        }

        for (int i = 0; i < args.size(); i++) {
            args.get(i).writeKDL(writer);
            if (i < args.size() - 1 || !props.isEmpty() || child.isPresent()) {
                writer.write(' ');
            }
        }

        final ArrayList<String> keys = new ArrayList<>(props.keySet());
        for (int i = 0; i < keys.size(); i++) {
            PrintUtil.writeStringQuotedAppropriately(writer, keys.get(i), true);
            writer.write('=');
            props.get(keys.get(i)).writeKDL(writer);
            if (i < keys.size() - 1 || child.isPresent()) {
                writer.write(' ');
            }
        }

        if (child.isPresent()) {
            writer.write("{\n");
            child.get().writeKDL(writer, indent, depth + 1);
            for (int i = 0; i < indent * depth; i++) {
                writer.write(' ');
            }
            writer.write('}');
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<KDLValue> args = new ArrayList<>();
        private final Map<String, KDLValue> props = new ConcurrentHashMap<>();

        private String identifier = null;
        private Optional<KDLDocument> child = Optional.empty();

        public Builder setIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder setChild(KDLDocument child) {
            this.child = Optional.of(child);
            return this;
        }

        public Builder addArg(KDLValue value) {
            if (value == null) {
                value = KDLNull.INSTANCE;
            }

            args.add(value);
            return this;
        }

        public Builder addArg(String strValue) {
            final KDLValue value;
            if (strValue == null) {
                value = KDLNull.INSTANCE;
            } else {
                value = new KDLString(strValue);
            }

            args.add(value);
            return this;
        }

        public Builder addArg(BigDecimal bdValue) {
            final KDLValue value;
            if (bdValue == null) {
                value = KDLNull.INSTANCE;
            } else {
                value = new KDLNumber(bdValue, 10);
            }

            args.add(value);
            return this;
        }

        public Builder addArg(BigDecimal bdValue, int radix) {
            final KDLValue value;
            if (bdValue == null) {
                value = KDLNull.INSTANCE;
            } else {
                value = new KDLNumber(bdValue, radix);
            }

            args.add(value);
            return this;
        }

        public Builder addArg(long val) {
            args.add(new KDLNumber(new BigDecimal(val), 10));
            return this;
        }
        
        public Builder addArg(long val, int radix) {
            args.add(new KDLNumber(new BigDecimal(val), radix));
            return this;
        }

        public Builder addArg(double val) {
            args.add(new KDLNumber(new BigDecimal(val), 10));
            return this;
        }

        public Builder addArg(double val, int radix) {
            args.add(new KDLNumber(new BigDecimal(val), radix));
            return this;
        }
        
        public Builder addNullArg() {
            args.add(KDLNull.INSTANCE);
            return this;
        }
        
        public Builder addArg(boolean val) {
            args.add(val ? KDLBoolean.TRUE : KDLBoolean.FALSE);
            return this;
        }

        public Builder addProp(String key, KDLValue value) {
            if (value == null) {
                value = KDLNull.INSTANCE;
            }

            props.put(key, value);
            return this;
        }

        public Builder addProp(String key, String strValue) {
            final KDLValue value;
            if (strValue == null) {
                value = KDLNull.INSTANCE;
            } else {
                value = new KDLString(strValue);
            }

            props.put(key, value);
            return this;
        }

        public Builder addProp(String key, BigDecimal bdValue) {
            final KDLValue value;
            if (bdValue == null) {
                value = KDLNull.INSTANCE;
            } else {
                value = new KDLNumber(bdValue, 10);
            }

            props.put(key, value);
            return this;
        }

        public Builder addProp(String key, BigDecimal bdValue, int radix) {
            final KDLValue value;
            if (bdValue == null) {
                value = KDLNull.INSTANCE;
            } else {
                value = new KDLNumber(bdValue, radix);
            }

            props.put(key, value);
            return this;
        }

        public Builder addProp(String key, int val) {
            props.put(key, new KDLNumber(new BigDecimal(val), 10));
            return this;
        }

        public Builder addProp(String key, int val, int radix) {
            props.put(key, new KDLNumber(new BigDecimal(val), radix));
            return this;
        }

        public Builder addProp(String key, double val) {
            props.put(key, new KDLNumber(new BigDecimal(val), 10));
            return this;
        }

        public Builder addProp(String key, double val, int radix) {
            props.put(key, new KDLNumber(new BigDecimal(val), radix));
            return this;
        }

        public Builder addNullProp(String key) {
            props.put(key, KDLNull.INSTANCE);
            return this;
        }

        public Builder addProp(String key, boolean val) {
            props.put(key, val ? KDLBoolean.TRUE : KDLBoolean.FALSE);
            return this;
        }
        
        public KDLNode build() {
            Objects.requireNonNull(identifier, "Identifier must be set");

            return new KDLNode(identifier, new HashMap<>(props), new ArrayList<>(args), child);
        }

    }

    @Override
    public String toString() {
        return "KDLNode{" +
                "identifier=" + identifier +
                ", props=" + props +
                ", args=" + args +
                ", child=" + child +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KDLNode)) return false;
        KDLNode kdlNode = (KDLNode) o;
        return Objects.equals(identifier, kdlNode.identifier) && Objects.equals(props, kdlNode.props) && Objects.equals(args, kdlNode.args) && Objects.equals(child, kdlNode.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, props, args, child);
    }
}
