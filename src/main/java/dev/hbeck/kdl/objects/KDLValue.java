package dev.hbeck.kdl.objects;

import java.util.Optional;
import java.math.BigDecimal;
import java.math.BigInteger;

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

    static KDLValue from(Object o) {
        if (o == null) return KDLNull.INSTANCE;
        if (o instanceof Boolean) {
            if ((Boolean) o) return KDLBoolean.TRUE;
            return KDLBoolean.FALSE;
        }
        if (o instanceof BigInteger) {
            return new KDLNumber(new BigDecimal((BigInteger)o), 10);
        }
        if (o instanceof BigDecimal) {
            return new KDLNumber((BigDecimal)o, 10);
        }
        if (o instanceof Number) {
            return new KDLNumber(new BigDecimal(o.toString()), 10);
        }
        if (o instanceof String) {
            return new KDLString((String)o);
        }
        if (o instanceof KDLValue) return (KDLValue)o;

        throw new RuntimeException("No KDLValue for object " + o.toString());
    }
}
