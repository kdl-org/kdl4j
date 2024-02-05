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
public class TestParseIdentifier {
    public TestParseIdentifier(String input, String expectedResult) {
        this.input = input;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"r", "r"},
                new Object[]{"bare", "bare"},
                new Object[]{"ぁ", "ぁ"},
                new Object[]{"-r", "-r"},
                new Object[]{"-1", "-1"}, //Yes, really. Should it be is another question

                new Object[]{"0hno", null},
                new Object[]{"=no", null},
                new Object[]{"", null}
        ).collect(Collectors.toList());
    }

    private final String input;
    private final String expectedResult;

    @Test
    public void doTest() throws IOException {
        final KDLParseContext context = TestUtil.strToContext(input);

        try {
            final String str = TestUtil.parser.parseBareIdentifier(context);
            assertThat(str, equalTo(expectedResult));
        } catch (KDLParseException | KDLInternalException e) {
            if (expectedResult != null) {
                throw new KDLParseException("Expected no errors", e);
            }
        }
    }
}
