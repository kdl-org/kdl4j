package dev.hbeck.kdl.objects;

import dev.hbeck.kdl.print.PrintConfig;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.Optional;

public class KDLBoolean implements KDLValue {
    public static final KDLBoolean TRUE = new KDLBoolean(true);
    public static final KDLBoolean FALSE = new KDLBoolean(false);

    private static final KDLString TRUE_STR = KDLString.from("true");
    private static final KDLString FALSE_STR = KDLString.from("false");

    private final boolean value;

    private KDLBoolean(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public void writeKDL(Writer writer, PrintConfig printConfig) throws IOException {
        writer.write(value ? "true" : "false");
    }

    @Override
    public KDLString getAsString() {
        return value ? TRUE_STR : FALSE_STR;
    }

    @Override
    public Optional<KDLNumber> getAsNumber() {
        return Optional.empty();
    }

    @Override
    public Optional<KDLBoolean> getAsBoolean() {
        return Optional.of(this);
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public String toString() {
        return "KDLBoolean{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KDLBoolean)) return false;
        KDLBoolean that = (KDLBoolean) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
