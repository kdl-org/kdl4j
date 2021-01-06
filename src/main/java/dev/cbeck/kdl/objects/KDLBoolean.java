package dev.cbeck.kdl.objects;

import java.util.Objects;

public class KDLBoolean implements KDLValue {
    public static final KDLBoolean TRUE = new KDLBoolean(true);
    public static final KDLBoolean FALSE = new KDLBoolean(false);

    private final boolean value;

    private KDLBoolean(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
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
