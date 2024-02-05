package kdl.parse;

import kdl.TestUtil;
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
public class TestParseEscapedString {
    public TestParseEscapedString(String input, String expectedResult) {
        this.input = input;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"\"\"", ""},
                new Object[]{"\"a\"", "a"},
                new Object[]{"\"a\nb\"", "a\nb"},
                new Object[]{"\"\\n\"", "\n"},
                new Object[]{"\"\\u{0001}\"", "\u0001"},
                new Object[]{"\"ぁ\"", "ぁ"},
                new Object[]{"\"\\u{3041}\"", "ぁ"},
                new Object[]{"\"", null},
                new Object[]{"", null}
        ).collect(Collectors.toList());
    }

    private final String input;
    private final String expectedResult;

    @Test
    public void doTest() throws IOException {
        final KDLParseContext context = TestUtil.strToContext(input);

        try {
            final String str = TestUtil.parser.parseEscapedString(context);
            assertThat(str, equalTo(expectedResult));
        } catch (KDLParseException | KDLInternalException e) {
            if (expectedResult != null) {
                throw new KDLParseException("Expected no errors", e);
            }
        }
    }
}
