package dev.hbeck.kdl.parse;

import dev.hbeck.kdl.TestUtil;
import dev.hbeck.kdl.objects.KDLNumber;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestParseNumber {
    public TestParseNumber(String input, KDLNumber expectedResult) {
        this.input = input;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"0", KDLNumber.from(0, 10)},
                new Object[]{"-0", KDLNumber.from(0, 10)},
                new Object[]{"1", KDLNumber.from(1, 10)},
                new Object[]{"01", KDLNumber.from(1, 10)},
                new Object[]{"10", KDLNumber.from(10, 10)},
                new Object[]{"1_0", KDLNumber.from(10, 10)},
                new Object[]{"-10", KDLNumber.from(-10, 10)},
                new Object[]{"+10", KDLNumber.from(10, 10)},
                new Object[]{"1.0", KDLNumber.from("1.0", 10).get()},
                new Object[]{"1e10", KDLNumber.from("1e10", 10).get()},
                new Object[]{"1e+10", KDLNumber.from("1e10", 10).get()},
                new Object[]{"1E+10", KDLNumber.from("1e10", 10).get()},
                new Object[]{"1e-10", KDLNumber.from("1e-10", 10).get()},
                new Object[]{"-1e-10", KDLNumber.from("-1e-10", 10).get()},
                new Object[]{"+1e-10", KDLNumber.from("1e-10", 10).get()},
                new Object[]{"0x0", KDLNumber.from(0, 16)},
                new Object[]{"0xFF", KDLNumber.from(255, 16)},
                new Object[]{"0xF_F", KDLNumber.from(255, 16)},
                new Object[]{"0o0", KDLNumber.from(0, 8)},
                new Object[]{"0o7", KDLNumber.from(7, 8)},
                new Object[]{"0o77", KDLNumber.from(63, 8)},
                new Object[]{"0o7_7", KDLNumber.from(63, 8)},
                new Object[]{"0b0", KDLNumber.from(0, 2)},
                new Object[]{"0b1", KDLNumber.from(1, 2)},
                new Object[]{"0b10", KDLNumber.from(2, 2)},
                new Object[]{"0b1_0", KDLNumber.from(2, 2)},
                new Object[]{"A", null},
                new Object[]{"_", null},
                new Object[]{"_1", null},
                new Object[]{"+_1", null},
                new Object[]{"0xRR", null},
                new Object[]{"0o8", null},
                new Object[]{"0b2", null}
        ).collect(Collectors.toList());
    }

    private final String input;
    private final KDLNumber expectedResult;

    @Test
    public void doTest() throws IOException {
        final KDLParseContext context = TestUtil.strToContext(input);

        try {
            final KDLNumber str = TestUtil.parser.parseNumber(context);
            assertThat(str, equalTo(expectedResult));
        } catch (KDLParseException e) {
            if (expectedResult != null) {
                throw new KDLParseException("Expected no errors", e);
            }
        }
    }
}
