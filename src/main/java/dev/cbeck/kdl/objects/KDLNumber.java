package dev.cbeck.kdl.objects;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Objects;

public class KDLNumber implements KDLValue {
    private final BigDecimal value;

    public KDLNumber(BigDecimal value) {
        this.value = Objects.requireNonNull(value);
    }

    public BigDecimal getAsBigDecimal() {
        return value;
    }

    @Override
    public void writeKDL(Writer writer) throws IOException {
        PrintUtil.writeNumber(writer, value);
    }

    @Override
    public String toString() {
        return "KDLNumber{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KDLNumber)) return false;
        KDLNumber kdlNumber = (KDLNumber) o;
        return Objects.equals(value, kdlNumber.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
