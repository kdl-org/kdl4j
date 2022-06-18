package dev.hbeck.kdl.objects;

import dev.hbeck.kdl.print.PrintConfig;
import dev.hbeck.kdl.print.PrintUtil;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * A model object representing a string in a KDL document. Note that even if quoted, identifiers are not KDLStrings.
 */
public class KDLString extends KDLValue<String> {
    private final String value;

    public KDLString(String value) {
        this(value, Optional.empty());
    }

    public KDLString(String value, Optional<String> type) {
        super(type);
        this.value = Objects.requireNonNull(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public KDLString getAsString() {
        return this;
    }

    @Override
    public Optional<KDLNumber> getAsNumber() {
        return KDLNumber.from(value, type);
    }

    @Override
    public Number getAsNumberOrElse(Number defaultValue) {
        return defaultValue;
    }

    @Override
    public Optional<KDLBoolean> getAsBoolean() {
        return KDLBoolean.fromString(value, type);
    }

    @Override
    public boolean getAsBooleanOrElse(boolean defaultValue) {
        return defaultValue;
    }

    @Override
    protected void writeKDLValue(Writer writer, PrintConfig printConfig) throws IOException {
        PrintUtil.writeStringQuotedAppropriately(writer, value, false, printConfig);
    }

    @Override
    protected String toKDLValue() {
        return value;
    }

    public static KDLString from(String val) {
        return from(val, Optional.empty());
    }

    public static KDLString from(String val, Optional<String> type) {
        return new KDLString(val, type);
    }

    public static KDLString empty() {
        return empty(Optional.empty());
    }

    public static KDLString empty(Optional<String> type) {
        return new KDLString("", type);
    }

    @Override
    public String toString() {
        return "KDLString{" +
                "value='" + value + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KDLString)) return false;
        KDLString kdlString = (KDLString) o;
        return Objects.equals(value, kdlString.value) && Objects.equals(type, kdlString.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }
}
