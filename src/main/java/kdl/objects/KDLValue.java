package kdl.objects;

import kdl.print.PrintConfig;
import kdl.print.PrintUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

public abstract class KDLValue<T> implements KDLObject {
    protected final Optional<String> type;

    public KDLValue(Optional<String> type) {
        this.type = type;
    }

    public abstract T getValue();

    public abstract KDLString getAsString();

    public abstract Optional<KDLNumber> getAsNumber();

    public abstract Number getAsNumberOrElse(Number defaultValue);

    public abstract Optional<KDLBoolean> getAsBoolean();

    public abstract boolean getAsBooleanOrElse(boolean defaultValue);

    protected abstract void writeKDLValue(Writer writer, PrintConfig printConfig) throws IOException;

    protected abstract String toKDLValue();

    public final Optional<String> getType() {
        return type;
    }

    public boolean isString() {
        return false;
    }

    public boolean isNumber() {
        return false;
    }

    public boolean isBoolean() {
        return false;
    }

    public boolean isNull() {
        return false;
    }

    public static KDLValue<?> from(Object o) {
        return from(o, Optional.empty());
    }

    public static KDLValue<?> from(Object o, Optional<String> type) {
        if (o == null) return new KDLNull(type);
        if (o instanceof Boolean) {
            return new KDLBoolean((Boolean) o, type);
        }
        if (o instanceof BigInteger) {
            return new KDLNumber(new BigDecimal((BigInteger)o), 10, type);
        }
        if (o instanceof BigDecimal) {
            return new KDLNumber((BigDecimal)o, 10, type);
        }
        if (o instanceof Number) {
            return new KDLNumber(new BigDecimal(o.toString()), 10, type);
        }
        if (o instanceof String) {
            return new KDLString((String) o, type);
        }
        if (o instanceof KDLValue) return (KDLValue<?>) o;

        throw new RuntimeException(String.format("No KDLValue for object '%s'", o));
    }

    @Override
    public final void writeKDL(Writer writer, PrintConfig printConfig) throws IOException {
        if (type.isPresent()) {
            writer.write('(');
            PrintUtil.writeStringQuotedAppropriately(writer, type.get(), true, printConfig);
            writer.write(')');
        }
        writeKDLValue(writer, printConfig);
    }

    @Override
    public final String toKDL() {
        final StringWriter writer = new StringWriter();
        if (type.isPresent()) {
            writer.write('(');
            try {
                PrintUtil.writeStringQuotedAppropriately(writer, type.get(), true, PrintConfig.PRETTY_DEFAULT);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to convert KDL value to KDL: '%s'", this), e);
            }
            writer.write(')');
        }

        writer.write(toKDLValue());

        return writer.toString();
    }
}
