package dev.hbeck.kdl.parse;

import dev.hbeck.kdl.TestUtil;
import dev.hbeck.kdl.objects.KDLBoolean;
import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLNull;
import dev.hbeck.kdl.objects.KDLString;
import dev.hbeck.kdl.objects.KDLValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestParseNode {
    public TestParseNode(String input, KDLNode expectedResult) {
        this.input = input;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"a", new KDLNode("a", map(), list(), Optional.empty())},
                new Object[]{"a\n", new KDLNode("a", map(), list(), Optional.empty())},
                new Object[]{"\"a\"", new KDLNode("a", map(), list(), Optional.empty())},
                new Object[]{"r\"a\"", new KDLNode("a", map(), list(), Optional.empty())},
                new Object[]{"r", new KDLNode("r", map(), list(), Optional.empty())},
                new Object[]{"rrrr", new KDLNode("rrrr", map(), list(), Optional.empty())},
                new Object[]{"a // stuff", new KDLNode("a", map(), list(), Optional.empty())},
                new Object[]{"a \"arg\"", new KDLNode("a", map(), list(new KDLString("arg")), Optional.empty())},
                new Object[]{"a key=\"val\"", new KDLNode("a", map("key", new KDLString("val")), list(), Optional.empty())},
                new Object[]{"a \"key\"=true", new KDLNode("a", map("key", KDLBoolean.TRUE), list(), Optional.empty())},
                new Object[]{"a \"arg\" key=\"val\"", new KDLNode("a", map("key", new KDLString("val")), list(new KDLString("arg")), Optional.empty())},
                new Object[]{"a r#\"arg\"\"# key=\"val\"", new KDLNode("a", map("key", new KDLString("val")), list(new KDLString("arg\"")), Optional.empty())},
                new Object[]{"a true false null", new KDLNode("a", map(), list(KDLBoolean.TRUE, KDLBoolean.FALSE, KDLNull.INSTANCE), Optional.empty())},
                new Object[]{"a /- \"arg1\" \"arg2\"", new KDLNode("a", map(), list(new KDLString("arg2")), Optional.empty())},
                new Object[]{"a key=\"val\" key=\"val2\"", new KDLNode("a", map("key", new KDLString("val2")), list(), Optional.empty())},
                new Object[]{"a key=\"val\" /- key=\"val2\"", new KDLNode("a", map("key", new KDLString("val")), list(), Optional.empty())},
                new Object[]{"a {}", new KDLNode("a", map(), list(), Optional.of(new KDLDocument(nodeList())))},
                new Object[]{"a {\nb\n}", new KDLNode("a", map(), list(), Optional.of(new KDLDocument(nodeList(new KDLNode("b", map(), list(), Optional.empty())))))},
                new Object[]{"a \"arg\" key=null \\\n{\nb\n}", new KDLNode("a", map("key", KDLNull.INSTANCE), list(new KDLString("arg")),
                        Optional.of(new KDLDocument(nodeList(new KDLNode("b", map(), list(), Optional.empty())))))},
                new Object[]{"a {\n\n}", new KDLNode("a", map(), list(), Optional.of(new KDLDocument(nodeList())))},
                new Object[]{"a{\n\n}", new KDLNode("a", map(), list(), Optional.of(new KDLDocument(nodeList())))},
                new Object[]{"a\"arg\"", null},
                new Object[]{"a#", null},
                new Object[]{"a /-", null}
        ).collect(Collectors.toList());
    }

    private final String input;
    private final KDLNode expectedResult;

    @Test
    public void doTest() throws IOException {
        final KDLParseContext context = TestUtil.strToContext(input);

        try {
            final KDLNode val = TestUtil.parser.parseNode(context).get();
            assertThat(val, equalTo(expectedResult));
        } catch (KDLParseException e) {
            if (expectedResult != null) {
                throw new KDLParseException("Expected no errors", e);
            }
        }
    }

    private static Map<String, KDLValue> map() {
        return new HashMap<>();
    }

    private static Map<String, KDLValue> map(String key, KDLValue value) {
        final HashMap<String, KDLValue> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private static Map<String, KDLValue> map(String key1, KDLValue value1, String key2, KDLValue value2) {
        final HashMap<String, KDLValue> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    private static List<KDLValue> list(KDLValue... values) {
        return Arrays.stream(values).collect(Collectors.toList());
    }

    private static List<KDLNode> nodeList(KDLNode... values) {
        return Arrays.stream(values).collect(Collectors.toList());
    }
}
