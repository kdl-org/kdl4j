package dev.hbeck.kdl.objects;

import java.io.IOException;
import java.io.Writer;

public class KDLNull implements KDLValue {
    public static final KDLNull INSTANCE = new KDLNull();

    private KDLNull() {
    }

    @Override
    public void writeKDL(Writer writer) throws IOException {
        writer.write("null");
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
