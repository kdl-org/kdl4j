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
public class TestParseRawString {
    public TestParseRawString(String input, String expectedResult, String expectedRemainder) {
        this.input = input;
        this.expectedResult = expectedResult;
        this.expectedRemainder = expectedRemainder;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"r\"\"", "", ""},
                new Object[]{"r\"\n\"", "\n", ""},
                new Object[]{"r\"\\n\"", "\\n", ""},
                new Object[]{"r\"\\u{0001}\"", "\\u{0001}", ""},
                new Object[]{"r#\"\"#", "", ""},
                new Object[]{"r#\"a\"#", "a", ""},
                new Object[]{"r##\"\"#\"##", "\"#", ""},
                new Object[]{"\"\"", null, "\""},
                new Object[]{"r", null, ""},
                new Object[]{"r\"", null, ""},
                new Object[]{"r#\"a\"##", null, ""},
                new Object[]{"", null, ""}
        ).collect(Collectors.toList());
    }

    private final String input;
    private final String expectedResult;
    private final String expectedRemainder;

    @Test
    public void doTest() throws IOException {
        final KDLParseContext context = TestUtil.strToContext(input);

        try {
            final String str = TestUtil.parser.parseRawString(context);
            assertThat(str, equalTo(expectedResult));
        } catch (KDLParseException | KDLInternalException e) {
            if (expectedResult != null) {
                throw new KDLParseException("Expected no errors", e);
            }
        }

        final String rem = TestUtil.readRemainder(context);
        assertThat(rem, equalTo(expectedRemainder));
    }
}
