package dev.hbeck.kdl.objects;

import dev.hbeck.kdl.print.PrintConfig;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public class KDLBoolean extends KDLValue<Boolean> {
    private final boolean value;

    public KDLBoolean(boolean value) {
        this(value, Optional.empty());
    }

    public KDLBoolean(boolean value, Optional<String> type) {
        super(type);
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }

    @Override
    public KDLString getAsString() {
        return value ? new KDLString("true", type) : new KDLString("false", type);
    }

    @Override
    public Optional<KDLNumber> getAsNumber() {
        return Optional.empty();
    }

    @Override
    public BigDecimal getAsNumber(BigDecimal defaultValue) {
        return defaultValue;
    }

    @Override
    public Optional<KDLBoolean> getAsBoolean() {
        return Optional.of(this);
    }

    @Override
    public boolean getAsBoolean(boolean defaultValue) {
        return value;
    }

    @Override
    protected void writeKDLValue(Writer writer, PrintConfig printConfig) throws IOException {
        writer.write(value ? "true" : "false");
    }

    @Override
    protected String toKDLValue() {
        return value ? "true" : "false";
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    public static Optional<KDLBoolean> fromString(String str, Optional<String> type) {
        if ("true".equals(str)) {
            return Optional.of(new KDLBoolean(true, type));
        } else if ("false".equals(str)) {
            return Optional.of(new KDLBoolean(false, type));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "KDLBoolean{" +
                "value=" + value +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KDLBoolean)) return false;
        KDLBoolean that = (KDLBoolean) o;
        return value == that.value && type.equals(that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
