package dev.hbeck.kdl.objects;

import java.util.Optional;

public interface KDLValue extends KDLObject {
    KDLString getAsString();

    Optional<KDLNumber> getAsNumber();

    Optional<KDLBoolean> getAsBoolean();

    default boolean isString() {
        return false;
    }

    default boolean isNumber() {
        return false;
    }

    default boolean isBoolean() {
        return false;
    }

    default boolean isNull() {
        return false;
    }
}
