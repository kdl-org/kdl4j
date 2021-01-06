package dev.cbeck.kdl.objects;

import java.util.Objects;
import java.util.Optional;

public class KDLProperty implements KDLObject {
    private final KDLIdentifier key;
    private final Optional<KDLValue> value;

    public KDLProperty(KDLIdentifier key, Optional<KDLValue> value) {
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
    }

    public KDLIdentifier getKey() {
        return key;
    }

    public Optional<KDLValue> getValue() {
        return value;
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
