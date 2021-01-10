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
public class TestGetSlashAction {
    public TestGetSlashAction(String input, KDLParser.SlashAction expectedResult, String expectedRemainder) {
        this.input = input;
        this.expectedResult = expectedResult;
        this.expectedRemainder = expectedRemainder;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"// stuff\n", KDLParser.SlashAction.END_NODE, "\n"},
                new Object[]{"// stuff \r\n", KDLParser.SlashAction.END_NODE, "\r\n"},
                new Object[]{"/- stuff", KDLParser.SlashAction.SKIP_NEXT, " stuff"},
                new Object[]{"/* comment */", KDLParser.SlashAction.NOTHING, ""},
                new Object[]{"/* comment */", KDLParser.SlashAction.NOTHING, ""},
                new Object[]{"/**/", KDLParser.SlashAction.NOTHING, ""},
                new Object[]{"/*/**/*/", KDLParser.SlashAction.NOTHING, ""},
                new Object[]{"/*   /*  */*/", KDLParser.SlashAction.NOTHING, ""},
                new Object[]{"/* ", null, ""},
                new Object[]{"/? ", null, "? "}
        ).collect(Collectors.toList());
    }

    private final String input;
    private final KDLParser.SlashAction expectedResult;
    private final String expectedRemainder;

    @Test
    public void doTest() throws IOException {
        final KDLParseContext context = TestUtil.strToContext(input);

        try {
            final KDLParser.SlashAction action = TestUtil.parser.getSlashAction(context);
            final String rem = TestUtil.readRemainder(context);

            assertThat(action, equalTo(expectedResult));
            assertThat(rem, equalTo(expectedRemainder));
        } catch (KDLParseException e) {
            if (expectedResult != null) {
                throw new KDLParseException("Expected no errors", e);
            }
        }
    }
}
