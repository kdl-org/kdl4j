package kdl.objects;

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
import java.util.function.Predicate;
import kdl.print.PrintConfig;

import static kdl.print.PrintUtil.writeStringQuotedAppropriately;

public class KDLNode implements KDLObject {
    private final String identifier;
    private final Optional<String> type;
    private final Map<String, KDLValue<?>> props;
    private final List<KDLValue<?>> args;
    private final Optional<KDLDocument> child;

    public KDLNode(String identifier, Optional<String> type, Map<String, KDLValue<?>> props, List<KDLValue<?>> args, Optional<KDLDocument> child) {
        this.identifier = Objects.requireNonNull(identifier);
        this.type = type;
        this.props = Collections.unmodifiableMap(Objects.requireNonNull(props));
        this.args = Collections.unmodifiableList(Objects.requireNonNull(args));
        this.child = Objects.requireNonNull(child);
    }

    /**
     * Get a builder used to gradually build a node
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public String getIdentifier() {
        return identifier;
    }

    public Optional<String> getType() {
        return type;
    }

    public Map<String, KDLValue<?>> getProps() {
        return props;
    }

    public List<KDLValue<?>> getArgs() {
        return args;
    }

    public Optional<KDLDocument> getChild() {
        return child;
    }

    /**
     * Writes a text representation of the node to the provided writer
     *
     * @param writer      the writer to write to
     * @param printConfig configuration controlling how the node is written
     * @throws IOException if there's any error writing the node
     */
    @Override
    public void writeKDL(Writer writer, PrintConfig printConfig) throws IOException {
        writeKDLPretty(writer, 0, printConfig);
    }

    void writeKDLPretty(Writer writer, int depth, PrintConfig printConfig) throws IOException {
        if (type.isPresent()) {
            writer.write('(');
            writeStringQuotedAppropriately(writer, type.get(), true, printConfig);
            writer.write(')');
        }

        writeStringQuotedAppropriately(writer, identifier, true, printConfig);

		for (var value : this.args) {
			if (!(value instanceof KDLNull) || printConfig.shouldPrintNullArgs()) {
				writer.write(' ');
				value.writeKDL(writer, printConfig);
			}
		}

		for (var entry : props.entrySet()) {
			if (!(entry.getValue() instanceof KDLNull) || printConfig.shouldPrintNullProps()) {
				writer.write(' ');
				writeStringQuotedAppropriately(writer, entry.getKey(), true, printConfig);
				writer.write('=');
				entry.getValue().writeKDL(writer, printConfig);
			}
		}

        if (child.isPresent()) {
            if (!child.get().getNodes().isEmpty() || printConfig.shouldPrintEmptyChildren()) {
                writer.write(" {");
                writer.write(printConfig.getNewline());
                child.get().writeKDL(writer, depth + 1, printConfig);
                for (int i = 0; i < printConfig.getIndent() * depth; i++) {
                    writer.write(printConfig.getIndentChar());
                }
                writer.write('}');
            }
        }
    }

    /**
     * Get a builder initialized with the contents of the current node
     *
     * @return the new builder
     */
    public Builder toBuilder() {
        return builder()
                .setIdentifier(identifier)
                .setType(type.orElse(null))
                .addAllArgs(args)
                .addAllProps(props)
                .setChild(child);
    }

    @Override
    public String toString() {
        return "KDLNode{" +
                "identifier=" + identifier +
                ", type=" + type +
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
        return Objects.equals(identifier, kdlNode.identifier) && Objects.equals(type, kdlNode.type) && Objects.equals(props, kdlNode.props) && Objects.equals(args, kdlNode.args) && Objects.equals(child, kdlNode.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, type, props, args, child);
    }

    public static class Builder {
        private final List<KDLValue<?>> args = new ArrayList<>();
        private final Map<String, KDLValue<?>> props = new ConcurrentHashMap<>();

        private String identifier = null;
        private String type = null;
        private Optional<KDLDocument> child = Optional.empty();

        public Builder setIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setChild(KDLDocument child) {
            this.child = Optional.of(child);
            return this;
        }

        public Builder setChild(Optional<KDLDocument> child) {
            this.child = child;
            return this;
        }

        public Builder addArg(KDLValue<?> value) {
            args.add(value);
            return this;
        }

        public Builder insertArgAt(int position, KDLValue<?> value) {
            if (position < args.size()) {
                args.add(position, value);
            } else {
                while (args.size() < position - 1) {
                    args.add(new KDLNull());
                }
                args.add(value);
            }
            return this;
        }

        public Builder removeArgIf(Predicate<KDLValue<?>> argPredicate) {
            args.removeIf(argPredicate);
            return this;
        }

        public Builder removeArg(KDLValue<?> arg) {
            while (args.remove(arg)) ;
            return this;
        }

        public Builder removePropIf(Predicate<String> keyPredicate) {
            for (String key : props.keySet()) {
                if (keyPredicate.test(key)) {
                    props.remove(key);
                }
            }
            return this;
        }

        public Builder removeProp(String key) {
            props.remove(key);
            return this;
        }

        public Builder addArg(String strValue) {
            return addArg(strValue, Optional.empty());
        }

        public Builder addArg(String strValue, Optional<String> type) {
            final KDLValue<?> value;
            if (strValue == null) {
                value = new KDLNull(type);
            } else {
                value = new KDLString(strValue, type);
            }

            args.add(value);
            return this;
        }

        public Builder addArg(BigDecimal bdValue) {
            return addArg(bdValue, Optional.empty());
        }

        public Builder addArg(BigDecimal bdValue, Optional<String> type) {
            return addArg(bdValue, 10, type);
        }

        public Builder addArg(BigDecimal bdValue, int radix, Optional<String> type) {
            final KDLValue<?> value;
            if (bdValue == null) {
                value = new KDLNull(type);
            } else {
                value = new KDLNumber(bdValue, radix, type);
            }

            args.add(value);
            return this;
        }

        public Builder addArg(long val) {
            return addArg(val, 10);
        }

        public Builder addArg(long val, int radix) {
            return addArg(val, radix, Optional.empty());
        }

        public Builder addArg(long val, int radix, Optional<String> type) {
            args.add(new KDLNumber(new BigDecimal(val), radix, type));
            return this;
        }

        public Builder addArg(double val) {
            return addArg(val, 10);
        }

        public Builder addArg(double val, int radix) {
            return addArg(val, radix, Optional.empty());
        }

        public Builder addArg(double val, int radix, Optional<String> type) {
            args.add(new KDLNumber(new BigDecimal(val), radix, type));
            return this;
        }

        public Builder addNullArg() {
            return addNullArg(Optional.empty());
        }

        public Builder addNullArg(Optional<String> type) {
            args.add(new KDLNull(type));
            return this;
        }

        public Builder addArg(boolean val) {
            return addArg(val, Optional.empty());
        }

        public Builder addArg(boolean val, Optional<String> type) {
            args.add(new KDLBoolean(val, type));
            return this;
        }

        public Builder addAllArgs(List<KDLValue<?>> args) {
            this.args.addAll(args);
            return this;
        }

        public Builder addProp(String key, KDLValue<?> value) {
            if (value == null) {
                value = new KDLNull();
            }

            props.put(key, value);
            return this;
        }

        public Builder addProp(String key, String strValue) {
            return addProp(key, strValue, Optional.empty());
        }

        public Builder addProp(String key, String strValue, Optional<String> type) {
            final KDLValue<?> value;
            if (strValue == null) {
                value = new KDLNull(type);
            } else {
                value = new KDLString(strValue, type);
            }

            props.put(key, value);
            return this;
        }

        public Builder addProp(String key, BigDecimal bdValue) {
            return addProp(key, bdValue, Optional.empty());
        }

        public Builder addProp(String key, BigDecimal bdValue, Optional<String> type) {
            final KDLValue<?> value;
            if (bdValue == null) {
                value = new KDLNull(type);
            } else {
                value = new KDLNumber(bdValue, 10, type);
            }

            props.put(key, value);
            return this;
        }

        public Builder addProp(String key, BigDecimal bdValue, int radix) {
            return addProp(key, bdValue, radix, Optional.empty());
        }

        public Builder addProp(String key, BigDecimal bdValue, int radix, Optional<String> type) {
            final KDLValue<?> value;
            if (bdValue == null) {
                value = new KDLNull(type);
            } else {
                value = new KDLNumber(bdValue, radix, type);
            }

            props.put(key, value);
            return this;
        }

        public Builder addProp(String key, int val) {
            return addProp(key, val, Optional.empty());
        }

        public Builder addProp(String key, int val, Optional<String> type) {
            return addProp(key, val, 10, type);
        }

        public Builder addProp(String key, int val, int radix) {
            return addProp(key, val, radix, Optional.empty());
        }

        public Builder addProp(String key, int val, int radix, Optional<String> type) {
            props.put(key, new KDLNumber(new BigDecimal(val), radix, type));
            return this;
        }

        public Builder addProp(String key, double val) {
            return addProp(key, val, Optional.empty());
        }

        public Builder addProp(String key, double val, Optional<String> type) {
            return addProp(key, val, 10, type);
        }

        public Builder addProp(String key, double val, int radix) {
            return addProp(key, val, radix, Optional.empty());
        }

        public Builder addProp(String key, double val, int radix, Optional<String> type) {
            props.put(key, new KDLNumber(new BigDecimal(val), radix, type));
            return this;
        }

        public Builder addProp(String key, boolean val) {
            return addProp(key, val, Optional.empty());
        }

        public Builder addProp(String key, boolean val, Optional<String> type) {
            props.put(key, new KDLBoolean(val, type));
            return this;
        }

        public Builder addProp(KDLProperty prop) {
            props.put(prop.getKey(), prop.getValue());
            return this;
        }

        public Builder addAllProps(Map<String, KDLValue<?>> props) {
            this.props.putAll(props);
            return this;
        }

        public Builder addNullProp(String key) {
            props.put(key, new KDLNull());
            return this;
        }

        public Builder clearArgs() {
            args.clear();
            return this;
        }

        public Builder clearProps() {
            props.clear();
            return this;
        }

        public KDLNode build() {
            Objects.requireNonNull(identifier, "Identifier must be set");

            return new KDLNode(identifier, Optional.ofNullable(type), new HashMap<>(props), new ArrayList<>(args), child);
        }
    }
}
