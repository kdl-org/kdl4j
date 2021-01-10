package dev.hbeck.kdl.parse;

import dev.hbeck.kdl.TestUtil;
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
public class TestParseArgOrProp {
    public TestParseArgOrProp(String input, String expectedResult) {
        this.input = input;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"bare", "bare"},
                new Object[]{"-10", "-10"},
                new Object[]{"r", "r"},
                new Object[]{"rrrr", "rrrr"},
                new Object[]{"r\"raw\"", "raw"},
                new Object[]{"#goals", null}
        ).collect(Collectors.toList());
    }

    private final String input;
    private final String expectedResult;

    @Test
    public void doTest() throws IOException {
        final KDLParseContext context = TestUtil.strToContext(input);

        try {
            final String val = TestUtil.parser.parseIdentifier(context);
            assertThat(val, equalTo(expectedResult));
        } catch (KDLParseException e) {
            if (expectedResult != null) {
                throw new KDLParseException("Expected no errors", e);
            }
        }
    }
}
