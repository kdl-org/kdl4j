package dev.hbeck.kdl.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

public class KDLProperty implements KDLObject {
    private final String key;
    private final KDLValue value;

    public KDLProperty(String key, KDLValue value) {
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
    }

    public KDLValue getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    @Override
    public void writeKDL(Writer writer) throws IOException {
        PrintUtil.writeStringQuotedAppropriately(writer, key, true);
        writer.write('=');
        value.writeKDL(writer);
    }

    @Override
    public String toString() {
        return "KDLProperty{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KDLProperty)) return false;
        KDLProperty that = (KDLProperty) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
