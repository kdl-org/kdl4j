package dev.cbeck.kdl.objects;

import java.util.Objects;

public class KDLIdentifier implements KDLObject {
    private final String identifier;

    public KDLIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return "KDLIdentifier{" +
                "identifier='" + identifier + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KDLIdentifier)) return false;
        KDLIdentifier that = (KDLIdentifier) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
