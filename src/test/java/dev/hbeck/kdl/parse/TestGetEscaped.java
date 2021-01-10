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
    public TestGetEscaped(String input, int expectedResult) {
        this.input = input;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"n", '\n'},
                new Object[]{"r", '\r'},
                new Object[]{"t", '\t'},
                new Object[]{"\\", '\\'},
                new Object[]{"/", '/'},
                new Object[]{"\"", '\"'},
                new Object[]{"b", '\b'},
                new Object[]{"f", '\f'},
                new Object[]{"u{1}", '\u0001'},
                new Object[]{"u{01}", '\u0001'},
                new Object[]{"u{001}", '\u0001'},
                new Object[]{"u{001}", '\u0001'},
                new Object[]{"u{0001}", '\u0001'},
                new Object[]{"u{00001}", '\u0001'},
                new Object[]{"u{000001}", '\u0001'},
                new Object[]{"u{10FFFF}", 0x10FFFF},
                new Object[]{"i", -2},
                new Object[]{"ux", -2},
                new Object[]{"u{x}", -2},
                new Object[]{"u{0001", -2},
                new Object[]{"u{AX}", -2},
                new Object[]{"u{}", -2},
                new Object[]{"u{0000001}", -2},
                new Object[]{"u{110000}", -2}
        ).collect(Collectors.toList());
    }

    private final String input;
    private final int expectedResult;

    @Test
    public void doTest() throws IOException {
        final KDLParseContext context = TestUtil.strToContext(input);
        final int initial = context.read();

        try {
            final int result = TestUtil.parser.getEscaped(initial, context);
            assertThat(result, equalTo(expectedResult));
        } catch (KDLParseException e) {
            if (expectedResult > 0) {
                throw new KDLParseException("Expected no errors", e);
            }
        }
    }
}
