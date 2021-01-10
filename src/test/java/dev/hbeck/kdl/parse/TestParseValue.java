package dev.hbeck.kdl.parse;

import dev.hbeck.kdl.TestUtil;
import dev.hbeck.kdl.objects.KDLBoolean;
import dev.hbeck.kdl.objects.KDLNull;
import dev.hbeck.kdl.objects.KDLNumber;
import dev.hbeck.kdl.objects.KDLString;
import dev.hbeck.kdl.objects.KDLValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestParseValue {
    public TestParseValue(String input, KDLValue expectedResult) {
        this.input = input;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"0", new KDLNumber(new BigDecimal(0), 10)},
                new Object[]{"10", new KDLNumber(new BigDecimal(10), 10)},
                new Object[]{"-10", new KDLNumber(new BigDecimal(-10), 10)},
                new Object[]{"+10", new KDLNumber(new BigDecimal(10), 10)},
                new Object[]{"\"\"", new KDLString("")},
                new Object[]{"\"r\"", new KDLString("r")},
                new Object[]{"\"\n\"", new KDLString("\n")},
                new Object[]{"\"\\n\"", new KDLString("\n")},
                new Object[]{"r\"\"", new KDLString("")},
                new Object[]{"r\"\n\"", new KDLString("\n")},
                new Object[]{"r\"\\n\"", new KDLString("\\n")},
                new Object[]{"true", KDLBoolean.TRUE},
                new Object[]{"false", KDLBoolean.FALSE},
                new Object[]{"null", KDLNull.INSTANCE},
                new Object[]{"\"true\"", new KDLString("true")},
                new Object[]{"\"false\"", new KDLString("false")},
                new Object[]{"\"null\"", new KDLString("null")},
                new Object[]{"garbage", null}
        ).collect(Collectors.toList());
    }

    private final String input;
    private final KDLValue expectedResult;

    @Test
    public void doTest() throws IOException {
        final KDLParseContext context = TestUtil.strToContext(input);

        try {
            final KDLValue val = TestUtil.parser.parseValue(context);
            assertThat(val, equalTo(expectedResult));
        } catch (KDLParseException e) {
            if (expectedResult != null) {
                throw new KDLParseException("Expected no errors", e);
            }
        }
    }
}
