package dev.hbeck.kdl.objects;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Objects;

public class KDLNumber implements KDLValue {
    private final BigDecimal value;
    private final int radix;

    public KDLNumber(BigDecimal value, int radix) {
        this.value = Objects.requireNonNull(value);
        this.radix = radix;
    }

    public BigDecimal getAsBigDecimal() {
        return value;
    }

    @Override
    public void writeKDL(Writer writer) throws IOException {
        switch (radix) {
            case 10:
                writer.write(value.toString());
                break;
            case 2:
                writer.write("0b");
                writer.write(value.toBigIntegerExact().toString(radix));
                break;
            case 8:
                writer.write("0o");
                writer.write(value.toBigIntegerExact().toString(radix));
                break;
            case 16:
                writer.write("0x");
                writer.write(value.toBigIntegerExact().toString(radix));
                break;
        }

    }

    @Override
    public String toString() {
        return "KDLNumber{" +
                "value=" + value +
                ", radix=" + radix +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KDLNumber)) return false;
        KDLNumber kdlNumber = (KDLNumber) o;
        return radix == kdlNumber.radix && Objects.equals(value, kdlNumber.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, radix);
    }
}
