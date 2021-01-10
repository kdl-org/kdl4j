package dev.hbeck.kdl.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.Optional;

public class KDLString implements KDLValue {
    public static final KDLString EMPTY = KDLString.from("");

    private final String value;

    public KDLString(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public void writeKDL(Writer writer) throws IOException {
        PrintUtil.writeStringQuotedAppropriately(writer, value, false);
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
        return KDLNumber.from(value);
    }

    @Override
    public Optional<KDLBoolean> getAsBoolean() {
        return Optional.empty();
    }

    public static KDLString from(String val) {
        return new KDLString(val);
    }

    public static KDLString empty() {
        return EMPTY;
    }

    @Override
    public String toString() {
        return "KDLString{" +
                "value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KDLString)) return false;
        KDLString kdlString = (KDLString) o;
        return Objects.equals(value, kdlString.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
