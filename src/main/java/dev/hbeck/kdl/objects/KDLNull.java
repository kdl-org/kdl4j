package dev.hbeck.kdl.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

public class KDLNull implements KDLValue {
    public static final KDLNull INSTANCE = new KDLNull();

    private static final KDLString AS_STR = KDLString.from("null");

    private KDLNull() {
    }

    @Override
    public void writeKDL(Writer writer) throws IOException {
        writer.write("null");
    }

    @Override
    public KDLString getAsString() {
        return AS_STR;
    }

    @Override
    public Optional<KDLNumber> getAsNumber() {
        return Optional.empty();
    }

    @Override
    public Optional<KDLBoolean> getAsBoolean() {
        return Optional.empty();
    }

    @Override
    public boolean isNull() {
        return true;
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof KDLNull;
    }

    public int hashCode() {
        return 0;
    }
}
