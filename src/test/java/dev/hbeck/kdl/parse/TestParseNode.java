package dev.hbeck.kdl.parse;

import dev.hbeck.kdl.TestUtil;
import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
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
public class TestParseNode {
    public TestParseNode(String input, KDLNode expectedResult) {
        this.input = input;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters(name = "{0} -> {1}")
    public static List<Object[]> getCases() {
        return Stream.of(
                new Object[]{"a", KDLNode.builder().setIdentifier("a").build()},
                new Object[]{"a\n", KDLNode.builder().setIdentifier("a").build()},
                new Object[]{"\"a\"", KDLNode.builder().setIdentifier("a").build()},
                new Object[]{"r\"a\"", KDLNode.builder().setIdentifier("a").build()},
                new Object[]{"r", KDLNode.builder().setIdentifier("r").build()},
                new Object[]{"rrrr", KDLNode.builder().setIdentifier("rrrr").build()},
                new Object[]{"a // stuff", KDLNode.builder().setIdentifier("a").build()},
                new Object[]{"a \"arg\"", KDLNode.builder().setIdentifier("a").addArg("arg").build()},
                new Object[]{"a key=\"val\"", KDLNode.builder().setIdentifier("a").addProp("key", "val").build()},
                new Object[]{"a \"key\"=true", KDLNode.builder().setIdentifier("a").addProp("key", true).build()},
                new Object[]{"a \"arg\" key=\"val\"", KDLNode.builder().setIdentifier("a").addProp("key", "val").addArg("arg").build()},
                new Object[]{"a r#\"arg\"\"# key=\"val\"", KDLNode.builder().setIdentifier("a").addProp("key", "val").addArg("arg\"").build()},
                new Object[]{"a true false null", KDLNode.builder().setIdentifier("a").addArg(true).addArg(false).addNullArg().build()},
                new Object[]{"a /- \"arg1\" \"arg2\"", KDLNode.builder().setIdentifier("a").addArg("arg2").build()},
                new Object[]{"a key=\"val\" key=\"val2\"", KDLNode.builder().setIdentifier("a").addProp("key", "val2").build()},
                new Object[]{"a key=\"val\" /- key=\"val2\"", KDLNode.builder().setIdentifier("a").addProp("key", "val").build()},
                new Object[]{"a {}", KDLNode.builder().setIdentifier("a").setChild(KDLDocument.empty()).build()},
                new Object[]{"a {\nb\n}", KDLNode.builder().setIdentifier("a")
                        .setChild(KDLDocument.builder().addNode(KDLNode.builder().setIdentifier("b").build()).build()).build()},
                new Object[]{"a \"arg\" key=null \\\n{\nb\n}", KDLNode.builder().setIdentifier("a").addArg("arg").addNullProp("key")
                        .setChild(KDLDocument.builder().addNode(KDLNode.builder().setIdentifier("b").build()).build()).build()},
                new Object[]{"a {\n\n}", KDLNode.builder().setIdentifier("a").setChild(KDLDocument.empty()).build()},
                new Object[]{"a{\n\n}", KDLNode.builder().setIdentifier("a").setChild(KDLDocument.empty()).build()},
                new Object[]{"a\"arg\"", null},
                new Object[]{"a=", null},
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
}
