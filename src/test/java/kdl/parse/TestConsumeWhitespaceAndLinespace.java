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
public class TestConsumeWhitespaceAndLinespace {
    public TestConsumeWhitespaceAndLinespace(String input, KDLParser.WhitespaceResult expectedResult, String expectedRemainder) {
        this.input = input;
        this.expectedResult = expectedResult;
        this.expectedRemainder = expectedRemainder;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"", KDLParser.WhitespaceResult.END_NODE, ""},
                new Object[]{"\n", KDLParser.WhitespaceResult.END_NODE, ""},
                new Object[]{"   \n/- a", KDLParser.WhitespaceResult.SKIP_NEXT, "a"},
                new Object[]{"\\\r\na", KDLParser.WhitespaceResult.NODE_SPACE, "a"},
                new Object[]{" \\\r\n \\\n \\\ra", KDLParser.WhitespaceResult.NODE_SPACE, "a"},
                new Object[]{" a ", KDLParser.WhitespaceResult.NODE_SPACE, "a "},
                new Object[]{"a", KDLParser.WhitespaceResult.NO_WHITESPACE, "a"},
                new Object[]{"\\\na", KDLParser.WhitespaceResult.NODE_SPACE, "a"},
                new Object[]{"\\\ra", KDLParser.WhitespaceResult.NODE_SPACE, "a"},
                new Object[]{"\t", KDLParser.WhitespaceResult.END_NODE, ""},
                new Object[]{"/* comment */a", KDLParser.WhitespaceResult.NODE_SPACE, "a"},
                new Object[]{"\t a", KDLParser.WhitespaceResult.NODE_SPACE, "a"},
                new Object[]{"\\ a", null, "a"},
                new Object[]{"/- ", null, ""},
                new Object[]{"/- \n", null, "\n"}
        ).collect(Collectors.toList());
    }

    private final String input;
    private final KDLParser.WhitespaceResult expectedResult;
    private final String expectedRemainder;

    @Test
    public void doTest() throws IOException {
        final KDLParseContext context = TestUtil.strToContext(input);

        try {
            final KDLParser.WhitespaceResult whitespaceResult = TestUtil.parser.consumeWhitespaceAndLinespace(context);
            assertThat(whitespaceResult, equalTo(expectedResult));
        } catch (KDLParseException e) {
            if (expectedResult != null) {
                throw new KDLParseException("Expected no errors", e);
            }
        }

        final String rem = TestUtil.readRemainder(context);
        assertThat(rem, equalTo(expectedRemainder));
    }
}
