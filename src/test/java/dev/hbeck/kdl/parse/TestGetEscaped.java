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
public class TestGetEscaped {
    public TestGetEscaped(String input, String expectedResult) {
        this.input = input;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"n", "\n"},
                new Object[]{"r", "\r"},
                new Object[]{"t", "\t"},
                new Object[]{"\\", "\\"},
                new Object[]{"\"", "\""},
                new Object[]{"b", "\b"},
                new Object[]{"f", "\f"},
                new Object[]{"u{1}", "\u0001"},
                new Object[]{"u{01}", "\u0001"},
                new Object[]{"u{001}", "\u0001"},
                new Object[]{"u{001}", "\u0001"},
                new Object[]{"u{0001}", "\u0001"},
                new Object[]{"u{00001}", "\u0001"},
                new Object[]{"u{000001}", "\u0001"},
                new Object[]{"ux", ""},
                new Object[]{"u{x}", ""},
                new Object[]{"u{0001", ""},
                new Object[]{"u{AX}", ""},
                new Object[]{"u{}", ""},
                new Object[]{"u{0000001}", ""},
                new Object[]{"u{110000}", ""}
        ).collect(Collectors.toList());
    }

    private final String input;
    private final String expectedResult;

    @Test
    public void doTest() throws IOException {
        final KDLParseContext context = TestUtil.strToContext(input);
        final int initial = context.read();

        try {
            final String result = TestUtil.parser.getEscaped(initial, context);
            assertThat(result, equalTo(expectedResult));
        } catch (KDLParseException e) {
            if (expectedResult != "") {
                throw new KDLParseException("Expected no errors", e);
            }
        }
    }
}
