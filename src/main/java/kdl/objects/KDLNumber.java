package kdl.objects;

import kdl.parse.KDLParser;
import kdl.print.PrintConfig;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

/**
 * Representation of a KDL number. Numbers may be base 16, 10, 8, or 2 as stored in the radix field. Base 10 numbers may
 * be fractional, but all others are limited to integers. {@link KDLParser} reads in all numbers as
 * either {@link BigDecimal} (for decimal) or {@link BigInteger} (for non-decimal), so if you <i>absolutely</i> need
 * that precision then don't hesitate to instanceof and cast.
 */
public class KDLNumber extends KDLValue<Number> {
    private final Number value;
    private final int radix;

    public KDLNumber(Number value, int radix) {
        this(value, radix, Optional.empty());
    }

    public KDLNumber(Number value, int radix, Optional<String> type) {
        super(type);
        this.value = Objects.requireNonNull(value);
        this.radix = radix;
    }

    @Override
    public Number getValue() {
        return value;
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public KDLString getAsString() {
        return KDLString.from(value.toString(), type);
    }

    @Override
    public Optional<KDLNumber> getAsNumber() {
        return Optional.of(this);
    }

    @Override
    public Number getAsNumberOrElse(Number defaultValue) {
        return value;
    }

    @Override
    public Optional<KDLBoolean> getAsBoolean() {
        return Optional.empty();
    }

    @Override
    public boolean getAsBooleanOrElse(boolean defaultValue) {
        return defaultValue;
    }

    @Override
    protected void writeKDLValue(Writer writer, PrintConfig printConfig) throws IOException {
        if (printConfig.shouldRespectRadix()) {
            /*
            Print out a number while respecting radix!
            If it's binary, octal, or hex, this converts it to a `BigInteger` to format.
            This is kludge-y, but someone *might* for some reason want to have a more-than-64-bit number in KDL.
            I have no idea *why* you'd want that, but who am I to judge?
            Plus one of our test cases uses an 80-bit hex number so let's just roll with it lol
            ~LemmaEOF
             */
            switch (radix) {
                case 10:
                    writer.write(value.toString().replace('E', printConfig.getExponentChar()));
                    break;
                case 2:
                    writer.write("0b");
                    writer.write(new BigInteger(value.toString()).toString(radix));
                    break;
                case 8:
                    writer.write("0o");
                    writer.write(new BigInteger(value.toString()).toString(radix));
                    break;
                case 16:
                    writer.write("0x");
                    writer.write(new BigInteger(value.toString()).toString(radix));
                    break;
            }
        } else {
            writer.write(value.toString().replace('E', printConfig.getExponentChar()));
        }
    }

    @Override
    protected String toKDLValue() {
        return value.toString();
    }

    /**
     * Get the Zero value for a given radix, which must be one of [2, 8, 10, 16]
     *
     * @return a new number with the value 0 and the given radix
     */
    public static KDLNumber zero(int radix) {
        return zero(radix, Optional.empty());
    }
    public static KDLNumber zero(int radix, Optional<String> type) {
        switch (radix) {
            case 2:
            case 8:
            case 10:
            case 16:
                return new KDLNumber(BigDecimal.ZERO, radix, type);
            default:
                throw new RuntimeException("Radix must be one of: [2, 8, 10, 16]");
        }
    }

    public static KDLNumber from(Number val, int radix) {
        return from(val, radix, Optional.empty());
    }

    public static KDLNumber from(Number val, int radix, Optional<String> type) {
        return new KDLNumber(val, radix, type);
    }

    public static KDLNumber from(Number val) {
        return from(val, Optional.empty());
    }

    public static KDLNumber from(Number val, Optional<String> type) {
        return from(val, 10, type);
    }

    public static Optional<KDLNumber> from(String val, int radix) {
        return from(val, radix, Optional.empty());
    }

    public static Optional<KDLNumber> from(String val, int radix, Optional<String> type) {
        return from(val, type).filter(v -> v.radix == radix);
    }

    /**
     * Parse the provided string into a KDLNumber if possible.
     *
     * @return an optional wrapping the new KDLNumber if the parse was successful, or empty() if not
     */
    public static Optional<KDLNumber> from(String val) {
        return from(val, Optional.empty());
    }

    public static Optional<KDLNumber> from(String val, Optional<String> type) {
        if (val == null || val.length() == 0) {
            return Optional.empty();
        }

        final int radix;
        final String toParse;
        if (val.charAt(0) == '0') {
            if (val.length() == 1) {
                return Optional.of(KDLNumber.zero(10, type));
            }

            switch (val.charAt(1)) {
                case 'x':
                    radix = 16;
                    toParse = val.substring(2);
                    break;
                case 'o':
                    radix = 8;
                    toParse = val.substring(2);
                    break;
                case 'b':
                    radix = 2;
                    toParse = val.substring(2);
                    break;
                default:
                    radix = 10;
                    toParse = val;
            }
        } else {
            radix = 10;
            toParse = val;
        }

        BigDecimal parsed;
        try {
            if (radix == 10) {
                parsed = new BigDecimal(toParse);
            } else {
                parsed = new BigDecimal(new BigInteger(toParse, radix));
            }
        } catch (NumberFormatException e) {
            parsed = null;
        }

        return Optional.ofNullable(parsed).map(bd -> from(bd, radix, type));
    }

    @Override
    public String toString() {
        return "KDLNumber{" +
                "value=" + value +
                ", radix=" + radix +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KDLNumber)) return false;
        KDLNumber kdlNumber = (KDLNumber) o;
        /*
        Oh, right. You technically can't compare two `Number`s. Thaaaanks, Oracle.
        Ah, well. Just use `toString` and check if those are equal.
        Numbers in Java all stringify in the same way, so this should work fine.
        ~LemmaEOF
         */
        return radix == kdlNumber.radix && Objects.equals(value.toString(), kdlNumber.value.toString()) && Objects.equals(type, kdlNumber.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, radix, type);
    }
}
