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
public class TestConsumeWhitespace {
    public TestConsumeWhitespace(String input, boolean expectedResult, String expectedRemainder) {
        this.input = input;
        this.expectedResult = expectedResult;
        this.expectedRemainder = expectedRemainder;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"", false, ""},
                new Object[]{"\\\r\na", true, "a"},
                new Object[]{" \\\r\n \\\n \\\ra", true, "a"},
                new Object[]{" a ", true, "a "},
                new Object[]{"a", false, "a"},
                new Object[]{"\\\na", true, "a"},
                new Object[]{"\\\ra", true, "a"},
                new Object[]{"\na", false, "\na"},
                new Object[]{"\t", true, ""},
                new Object[]{"\t a", true, "a"}
        ).collect(Collectors.toList());
    }

    private final String input;
    private final boolean expectedResult;
    private final String expectedRemainder;

    @Test
    public void doTest() throws IOException {
        final KDLParseContext context = TestUtil.strToContext(input);
        final boolean result = TestUtil.parser.consumeWhitespace(context);
        final String rem = TestUtil.readRemainder(context);

        assertThat(result, equalTo(expectedResult));
        assertThat(rem, equalTo(expectedRemainder));
    }
}
