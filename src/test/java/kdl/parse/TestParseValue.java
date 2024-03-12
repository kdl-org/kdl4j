package kdl.parse;

import kdl.TestUtil;
import kdl.objects.*;
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
public class TestParseValue {
    public TestParseValue(String input, KDLValue expectedResult) {
        this.input = input;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"0", KDLNumber.from(0, 10)},
                new Object[]{"10", KDLNumber.from(10, 10)},
                new Object[]{"-10", KDLNumber.from(-10, 10)},
                new Object[]{"+10", KDLNumber.from(10, 10)},
                new Object[]{"\"\"", KDLString.from("")},
                new Object[]{"\"r\"", KDLString.from("r")},
                new Object[]{"\"\n\"", KDLString.from("\n")},
                new Object[]{"\"\\n\"", KDLString.from("\n")},
                new Object[]{"r\"\"", KDLString.from("")},
                new Object[]{"r\"\n\"", KDLString.from("\n")},
                new Object[]{"r\"\\n\"", KDLString.from("\\n")},
                new Object[]{"true", new KDLBoolean(true)},
                new Object[]{"false", new KDLBoolean(false)},
                new Object[]{"null", new KDLNull()},
                new Object[]{"\"true\"", KDLString.from("true")},
                new Object[]{"\"false\"", KDLString.from("false")},
                new Object[]{"\"null\"", KDLString.from("null")},
                new Object[]{"garbage", null}
        ).collect(Collectors.toList());
    }

    private final String input;
    private final KDLValue<?> expectedResult;

    @Test
    public void doTest() throws IOException {
        final KDLParseContext context = TestUtil.strToContext(input);

        try {
            final KDLValue<?> val = TestUtil.parser.parseValue(context);
            assertThat(val, equalTo(expectedResult));
        } catch (KDLParseException e) {
            if (expectedResult != null) {
                throw new KDLParseException("Expected no errors", e);
            }
        }
    }
}
