package dev.hbeck.kdl.objects;

import dev.hbeck.kdl.print.PrintConfig;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;

/**
 * A model object representing the KDL 'null' value.
 */
public class KDLNull implements KDLValue {
    public static final KDLNull INSTANCE = new KDLNull();

    private static final KDLString AS_KDL_STR = KDLString.from("null");

    /**
     * New instances should not be created, instead use the INSTANCE constant
     */
    private KDLNull() {
    }

    @Override
    public void writeKDL(Writer writer, PrintConfig printConfig) throws IOException {
        writer.write("null");
    }

    @Override
    public KDLString getAsString() {
        return AS_KDL_STR;
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
