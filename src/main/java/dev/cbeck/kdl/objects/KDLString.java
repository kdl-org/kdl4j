package dev.cbeck.kdl.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

public class KDLString implements KDLValue {
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
