package dev.hbeck.kdl.parse;

import dev.hbeck.kdl.objects.*;
import static dev.hbeck.kdl.TestUtil.parser;
import static dev.hbeck.kdl.TestUtil.throwsException;
import org.junit.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.Optional;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestParser {
    @Test
    public void test_parseEmptyString() {
        assertThat(parser.parse(""), equalTo(doc()));
        assertThat(parser.parse(" "), equalTo(doc()));
        assertThat(parser.parse("\n"), equalTo(doc()));
    }

    @Test
    public void test_nodes() {
        assertThat(parser.parse("node"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node\n"), equalTo(doc(node("node"))));
        assertThat(parser.parse("\nnode\n"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node1\nnode2"),
            equalTo(doc(node("node1"), node("node2"))));
    }

    @Test
    public void test_node() {
        assertThat(parser.parse("node;"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node 1"), equalTo(doc(node("node", List.of(1)))));
        assertThat(parser.parse("node 1 2 \"3\" true false null"),
            equalTo(doc(node("node", List.of(1, 2, "3", true, false, KDLNull.INSTANCE)))));
        assertThat(parser.parse("node {\n    node2\n}"),
            equalTo(doc(node("node", node("node2")))));
    }

    @Test
    public void test_slashDashComment() {
        assertThat(parser.parse("/-node"), equalTo(doc()));
        assertThat(parser.parse("/- node"), equalTo(doc()));
        assertThat(parser.parse("/- node\n"), equalTo(doc()));
        assertThat(parser.parse("/-node 1 2 3"), equalTo(doc()));
        assertThat(parser.parse("/-node key=false"), equalTo(doc()));
        assertThat(parser.parse("/-node{\nnode\n}"), equalTo(doc()));
        assertThat(parser.parse("/-node 1 2 3 key=\"value\" \\\n{\nnode\n}"), equalTo(doc()));
    }

    @Test
    public void test_argSlashdashComment() {
        assertThat(parser.parse("node /-1"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node /-1 2"), equalTo(doc(node("node", List.of(2)))));
        assertThat(parser.parse("node 1 /- 2 3"), equalTo(doc(node("node", List.of(1, 3)))));
        assertThat(parser.parse("node /--1"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node /- -1"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node \\\n/- -1"), equalTo(doc(node("node"))));
    }

    @Test
    public void test_prop_slashdash_comment() {
        assertThat(parser.parse("node /-key=1"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node /- key=1"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node key=1 /-key2=2"), equalTo(doc(node("node", Map.of("key", 1)))));
    }

    @Test
    public void test_childrenSlashdashComment() {
        assertThat(parser.parse("node /-{}"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node /- {}"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node /-{\nnode2\n}"), equalTo(doc(node("node"))));
    }

    @Test
    public void test_string() {
        assertThat(parser.parse("node \"\""), equalTo(doc(node("node", List.of("")))));
        assertThat(parser.parse("node \"hello\""), equalTo(doc(node("node", List.of("hello")))));
        assertThat(parser.parse("node \"hello\\nworld\""), equalTo(doc(node("node", List.of("hello\nworld")))));
        assertThat(parser.parse("node \"\\u{1F408}\""), equalTo(doc(node("node", List.of("\uD83D\uDC08")))));
        assertThat(parser.parse("node \"\\\"\\\\\\/\\b\\f\\n\\r\\t\""),
            equalTo(doc(node("node", List.of("\"\\/\u0008\u000C\n\r\t")))));
        assertThat(parser.parse("node \"\\u{10}\""), equalTo(doc(node("node", List.of("\u0010")))));

        assertThat(() -> parser.parse("node \"\\i\""), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node \"\\u{c0ffee}\""), throwsException(KDLParseException.class));
    }

    @Test
    public void test_float() {
        assertThat(parser.parse("node 1.0"), equalTo(doc(node("node", List.of(1.0)))));
        assertThat(parser.parse("node 0.0"), equalTo(doc(node("node", List.of(0.0)))));
        assertThat(parser.parse("node -1.0"), equalTo(doc(node("node", List.of(-1.0)))));
        assertThat(parser.parse("node +1.0"), equalTo(doc(node("node", List.of(1.0)))));
        assertThat(parser.parse("node 1.0e10"), equalTo(doc(node("node", List.of(1.0e10)))));
        assertThat(parser.parse("node 1.0e-10"), equalTo(doc(node("node", List.of(1.0e-10)))));
        assertThat(parser.parse("node 123_456_789.0"),
            equalTo(doc(node("node", List.of(new BigDecimal("123456789.0"))))));
        assertThat(parser.parse("node 123_456_789.0_"),
            equalTo(doc(node("node", List.of(new BigDecimal("123456789.0"))))));

        assertThat(() -> parser.parse("node ?1.0"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node _1.0"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node .0"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 1._0"), throwsException(KDLParseException.class)); // TODO: fails
        assertThat(() -> parser.parse("node 1."), throwsException(KDLParseException.class)); // TODO: fails
    }

    @Test
    public void test_integer() {
        assertThat(parser.parse("node 0"), equalTo(doc(node("node", List.of(0)))));
        assertThat(parser.parse("node 0123456789"), equalTo(doc(node("node", List.of(123456789)))));
        assertThat(parser.parse("node 0123_456_789"), equalTo(doc(node("node", List.of(123456789)))));
        assertThat(parser.parse("node 0123_456_789_"), equalTo(doc(node("node", List.of(123456789)))));
        assertThat(parser.parse("node +0123456789"), equalTo(doc(node("node", List.of(123456789)))));
        assertThat(parser.parse("node -0123456789"), equalTo(doc(node("node", List.of(-123456789)))));

        assertThat(() -> parser.parse("node ?0123456789"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node _0123456789"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node a"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node --"), throwsException(KDLParseException.class));
    }

    @Test
    public void test_hexadecimal() {
        KDLNumber kdlNumber = new KDLNumber(new BigDecimal(new BigInteger("0123456789abcdef", 16)), 16);

        assertThat(parser.parse("node 0x0123456789abcdef"), equalTo(doc(node("node", List.of(kdlNumber)))));
        assertThat(parser.parse("node 0x01234567_89abcdef"), equalTo(doc(node("node", List.of(kdlNumber)))));
        assertThat(parser.parse("node 0x01234567_89abcdef_"), equalTo(doc(node("node", List.of(kdlNumber)))));
        
        assertThat(() -> parser.parse("node 0x_123"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 0xg"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 0xx"), throwsException(KDLParseException.class));
    }

    @Test
    public void test_octal() {
        KDLNumber kdlNumber = new KDLNumber(new BigDecimal(01234567), 8);

        assertThat(parser.parse("node 0o01234567"), equalTo(doc(node("node", List.of(kdlNumber)))));
        assertThat(parser.parse("node 0o0123_4567"), equalTo(doc(node("node", List.of(kdlNumber)))));
        assertThat(parser.parse("node 0o01234567_"), equalTo(doc(node("node", List.of(kdlNumber)))));

        assertThat(() -> parser.parse("node 0o_123"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 0o8"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 0oo"), throwsException(KDLParseException.class));
    }

    @Test
    public void test_binary() {
        KDLNumber kdlNumber = new KDLNumber(new BigDecimal(6), 2);

        assertThat(parser.parse("node 0b0110"), equalTo(doc(node("node", List.of(kdlNumber)))));
        assertThat(parser.parse("node 0b01_10"), equalTo(doc(node("node", List.of(kdlNumber)))));
        assertThat(parser.parse("node 0b01___10"), equalTo(doc(node("node", List.of(kdlNumber)))));
        assertThat(parser.parse("node 0b0110_"), equalTo(doc(node("node", List.of(kdlNumber)))));

        assertThat(() -> parser.parse("node 0b_0110"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 0b20"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 0bb"), throwsException(KDLParseException.class));
    }

    @Test
    public void test_raw_string() {
        assertThat(parser.parse("node r\"foo\""), equalTo(doc(node("node", List.of("foo")))));
        assertThat(parser.parse("node r\"foo\\nbar\""), equalTo(doc(node("node", List.of("foo\\nbar")))));
        assertThat(parser.parse("node r#\"foo\"#"), equalTo(doc(node("node", List.of("foo")))));
        assertThat(parser.parse("node r##\"foo\"##"), equalTo(doc(node("node", List.of("foo")))));
        assertThat(parser.parse("node r\"\\nfoo\\r\""), equalTo(doc(node("node", List.of("\\nfoo\\r")))));
        assertThat(parser.parse("node r#\"hello\"world\"#"), equalTo(doc(node("node", List.of("hello\"world")))));
    
        assertThat(() -> parser.parse("node r##\"foo\"#"), throwsException(KDLParseException.class));
    }

    @Test
    public void test_boolean() {
        assertThat(parser.parse("node true"), equalTo(doc(node("node", List.of(true)))));
        assertThat(parser.parse("node false"), equalTo(doc(node("node", List.of(false)))));
    }

    @Test
    public void test_node_space() {
        assertThat(parser.parse("node 1"), equalTo(doc(node("node", List.of(1)))));
        assertThat(parser.parse("node\t1"), equalTo(doc(node("node", List.of(1)))));
        assertThat(parser.parse("node\t \\\n 1"), equalTo(doc(node("node", List.of(1)))));
        assertThat(parser.parse("node\t \\ // hello\n 1"), equalTo(doc(node("node", List.of(1))))); // TODO: fails
    }

    @Test
    public void test_single_line_comment() {
        assertThat(parser.parse("//hello"), equalTo(doc()));
        assertThat(parser.parse("// \thello"), equalTo(doc()));
        assertThat(parser.parse("//hello\n"), equalTo(doc()));
        assertThat(parser.parse("//hello\r\n"), equalTo(doc()));
        assertThat(parser.parse("//hello\n\r"), equalTo(doc()));
        assertThat(parser.parse("//hello\rworld"), equalTo(doc(node("world"))));
        assertThat(parser.parse("//hello\nworld\r\n"), equalTo(doc(node("world"))));
    }

    @Test
    public void test_multi_line_comment() {
        assertThat(parser.parse("/*hello*/"), equalTo(doc()));
        assertThat(parser.parse("/*hello*/\n"), equalTo(doc()));
        assertThat(parser.parse("/*\nhello\r\n*/"), equalTo(doc()));
        assertThat(parser.parse("/*\nhello** /\n*/"), equalTo(doc()));
        assertThat(parser.parse("/**\nhello** /\n*/"), equalTo(doc()));
        assertThat(parser.parse("/*hello*/world"), equalTo(doc(node("world"))));
    }

    @Test
    public void test_escline() {
        assertThat(parser.parse("\\\nfoo"), equalTo(doc(node("foo"))));
        assertThat(parser.parse("\\\n    foo"), equalTo(doc(node("foo"))));
        assertThat(parser.parse("\\    \t \nfoo"), equalTo(doc(node("foo")))); // TODO: fails
        assertThat(parser.parse("\\ // test \nfoo"), equalTo(doc(node("foo")))); // TODO: fails
        assertThat(parser.parse("\\ // test \n    foo"), equalTo(doc(node("foo")))); // TODO: fails
    }

    @Test
    public void test_whitespace() {
        assertThat(parser.parse(" node"), equalTo(doc(node("node"))));
        assertThat(parser.parse("\tnode"), equalTo(doc(node("node"))));
        assertThat(parser.parse("/* \nfoo\r\n */ etc"), equalTo(doc(node("etc"))));
    }

    @Test
    public void test_newline() {
        assertThat(parser.parse("node1\nnode2"), equalTo(doc(node("node1"), node("node2"))));
        assertThat(parser.parse("node1\rnode2"), equalTo(doc(node("node1"), node("node2"))));
        assertThat(parser.parse("node1\r\nnode2"), equalTo(doc(node("node1"), node("node2"))));
        assertThat(parser.parse("node1\n\nnode2"), equalTo(doc(node("node1"), node("node2"))));
    }

    @Test
    public void test_nestedChildNodes() {
        KDLDocument actual = parser.parse(
            "content { \n" +
            "    section \"First section\" {\n" +
            "        paragraph \"This is the first paragraph\"\n" +
            "        paragraph \"This is the second paragraph\"\n" +
            "    }\n" +
            "}"
        );

        KDLDocument expected = doc(
            node("content", 
                node("section", List.of("First section"),
                    node("paragraph", List.of("This is the first paragraph")),
                    node("paragraph", List.of("This is the second paragraph"))
                )
            )
        );

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void test_semicolon() {
        assertThat(parser.parse("node1; node2; node3"),
            equalTo(doc(node("node1"), node("node2"), node("node3"))));
        assertThat(parser.parse("node1 { node2; }; node3"),
            equalTo(doc(node("node1", node("node2")), node("node3")))); // TODO: fails
    }

    @Test
    public void test_multiline_strings() {
        assertThat(parser.parse("string \"my\nmultiline\nvalue\""),
            equalTo(doc(node("string", List.of("my\nmultiline\nvalue")))));
    }

    @Test
    public void test_comments() {
        KDLDocument actual = parser.parse(
            "// C style\n"+

            "/*\n" +
            "C style multiline\n" +
            "*/\n" +

            "tag /*foo=true*/ bar=false\n" +

            "/*/*\n" +
            "hello\n" +
            "*/*/"
        );

        KDLDocument expected = doc(
            node("tag", Map.of("bar", false))
        );

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void test_multiline_nodes() {
        KDLDocument actual = parser.parse(
            "title \\\n" +
            "    \"Some title\"\n" +

            "my-node 1 2 \\    // comments are ok after \\\n" +
            "        3 4\n"
        );

        KDLDocument expected = doc(
            node("title", List.of("Some title")),
            node("my-node", List.of(1, 2, 3, 4))
        );

        assertThat(actual, equalTo(expected)); // TODO: fails
    }

    @Test
    public void test_utf8() {
        assertThat(parser.parse("smile \"😁\""), equalTo(doc(node("smile", List.of("😁")))));
        assertThat(parser.parse("ノード お名前＝\"☜(ﾟヮﾟ☜)\""),
            equalTo(doc(node("ノード", Map.of("お名前", "☜(ﾟヮﾟ☜)"))))); // TODO: fails
    }

    @Test
    public void test_node_names() {
        assertThat(parser.parse("\"!@#$@$%Q#$%~@!40\" \"1.2.3\" \"!!!!!\"=true"),
            equalTo(doc(node("!@#$@$%Q#$%~@!40", List.of("1.2.3"), Map.of("!!!!!", true)))));
        assertThat(parser.parse("foo123~!@#$%^&*.:'|/?+ \"weeee\""),
            equalTo(doc(node("foo123~!@#$%^&*.:'|/?+", List.of("weeee"))))); // TODO: fails
    }

    private KDLDocument doc(KDLNode... nodes) {
        List<KDLNode> nodeList = new ArrayList<>();
        Collections.addAll(nodeList, nodes);
        return new KDLDocument(nodeList);
    }

    private KDLNode node(String ident, List<Object> args, Map<String, Object> props, KDLNode... nodes) {
        List<KDLValue> argValues = new ArrayList<>();
        for (Object o : args) {
            argValues.add(KDLValue.from(o));
        }
        Map<String, KDLValue> propValues = new HashMap<>();
        for (Map.Entry<String, Object> e : props.entrySet()) {
            propValues.put(e.getKey(), KDLValue.from(e.getValue()));
        }
        Optional<KDLDocument> children = Optional.empty();
        if (nodes.length > 0) {
            children = Optional.of(doc(nodes));
        }
        return new KDLNode(ident, propValues, argValues, children);
    }

    private KDLNode node(String ident, List<Object> args, KDLNode... nodes) {
        return node(ident, args, Collections.emptyMap(), nodes);
    }

    private KDLNode node(String ident, Map<String, Object> props, KDLNode... nodes) {
        return node(ident, Collections.emptyList(), props, nodes);
    }

    private KDLNode node(String ident, KDLNode... nodes) {
        return node(ident, Collections.emptyList(), nodes);
    }
}
