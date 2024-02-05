package kdl.parse;

import kdl.TestUtil;
import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestParseChild {
    public TestParseChild(String input, KDLDocument expectedResult) {
        this.input = input;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"{}", doc()},
                new Object[]{"{\n\n}", doc()},
                new Object[]{"{\na\n}", doc("a")},
                new Object[]{"{\n\na\n\nb\n}", doc("a", "b")},
                new Object[]{"{\na\nb\n}", doc("a", "b")},
                new Object[]{"", null},
                new Object[]{"{", null},
                new Object[]{"{\n", null},
                new Object[]{"{\na /-", null},
                new Object[]{"{\na\n/-", null}
        ).collect(Collectors.toList());
    }

    private final String input;
    private final KDLDocument expectedResult;

    @Test
    public void doTest() throws IOException {
        final KDLParseContext context = TestUtil.strToContext(input);

        try {
            final KDLDocument val = TestUtil.parser.parseChild(context);
            assertThat(val, equalTo(expectedResult));
        } catch (KDLParseException | KDLInternalException e) {
            if (expectedResult != null) {
                throw new KDLParseException("Expected no errors", e);
            }
        }
    }

    private static KDLDocument doc(String... identifiers) {
        final List<KDLNode> nodes = Arrays.stream(identifiers)
                .map(id -> KDLNode.builder().setIdentifier(id).build())
                .collect(Collectors.toList());
        return new KDLDocument(nodes);
    }
}
