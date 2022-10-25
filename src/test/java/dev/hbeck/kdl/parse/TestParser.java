package dev.hbeck.kdl.parse;

import dev.hbeck.kdl.objects.KDLBoolean;
import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLNull;
import dev.hbeck.kdl.objects.KDLNumber;
import dev.hbeck.kdl.objects.KDLString;
import dev.hbeck.kdl.objects.KDLValue;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.hbeck.kdl.TestUtil.parser;
import static dev.hbeck.kdl.TestUtil.throwsException;
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
        assertThat(parser.parse("node 1"), equalTo(doc(node("node", list(1)))));
        assertThat(parser.parse("node 1 2 \"3\" true false null"),
                equalTo(doc(node("node", list(1, 2, "3", true, false, new KDLNull())))));
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
        assertThat(parser.parse("node /-1 2"), equalTo(doc(node("node", list(2)))));
        assertThat(parser.parse("node 1 /- 2 3"), equalTo(doc(node("node", list(1, 3)))));
        assertThat(parser.parse("node /--1"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node /- -1"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node \\\n/- -1"), equalTo(doc(node("node"))));
    }

    @Test
    public void test_prop_slashdash_comment() {
        assertThat(parser.parse("node /-key=1"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node /- key=1"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node key=1 /-key2=2"), equalTo(doc(node("node", map("key", 1)))));
    }

    @Test
    public void test_childrenSlashdashComment() {
        assertThat(parser.parse("node /-{}"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node /- {}"), equalTo(doc(node("node"))));
        assertThat(parser.parse("node /-{\nnode2\n}"), equalTo(doc(node("node"))));
    }

    @Test
    public void test_string() {
        assertThat(parser.parse("node \"\""), equalTo(doc(node("node", list("")))));
        assertThat(parser.parse("node \"hello\""), equalTo(doc(node("node", list("hello")))));
        assertThat(parser.parse("node \"hello\\nworld\""), equalTo(doc(node("node", list("hello\nworld")))));
        assertThat(parser.parse("node \"\\u{1F408}\""), equalTo(doc(node("node", list("\uD83D\uDC08")))));
        assertThat(parser.parse("node \"\\\"\\\\\\/\\b\\f\\n\\r\\t\""),
                equalTo(doc(node("node", list("\"\\/\u0008\u000C\n\r\t")))));
        assertThat(parser.parse("node \"\\u{10}\""), equalTo(doc(node("node", list("\u0010")))));

        assertThat(() -> parser.parse("node \"\\i\""), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node \"\\u{c0ffee}\""), throwsException(KDLParseException.class));
    }

    @Test
    public void test_float() {
        assertThat(parser.parse("node 1.0"), equalTo(doc(node("node", list(1.0)))));
        assertThat(parser.parse("node 0.0"), equalTo(doc(node("node", list(0.0)))));
        assertThat(parser.parse("node -1.0"), equalTo(doc(node("node", list(-1.0)))));
        assertThat(parser.parse("node +1.0"), equalTo(doc(node("node", list(1.0)))));
        assertThat(parser.parse("node 1.0e10"), equalTo(doc(node("node", list(1.0e10)))));
        assertThat(parser.parse("node 1.0e-10"), equalTo(doc(node("node", list(1.0e-10)))));
        assertThat(parser.parse("node 123_456_789.0"),
                equalTo(doc(node("node", list(new BigDecimal("123456789.0"))))));
        assertThat(parser.parse("node 123_456_789.0_1"),
                equalTo(doc(node("node", list(new BigDecimal("123456789.01"))))));

        assertThat(() -> parser.parse("node ?1.0"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node _1.0"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node .0"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 1.0E100E10"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 1.0E1.10"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 1.0.0"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 1.0.0E7"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 1.E7"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 1._0"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 1."), throwsException(KDLParseException.class));
    }

    @Test
    public void test_integer() {
        assertThat(parser.parse("node 0"), equalTo(doc(node("node", list(0)))));
        assertThat(parser.parse("node 0123456789"), equalTo(doc(node("node", list(123456789)))));
        assertThat(parser.parse("node 0123_456_789"), equalTo(doc(node("node", list(123456789)))));
        assertThat(parser.parse("node 0123_456_789_"), equalTo(doc(node("node", list(123456789)))));
        assertThat(parser.parse("node +0123456789"), equalTo(doc(node("node", list(123456789)))));
        assertThat(parser.parse("node -0123456789"), equalTo(doc(node("node", list(-123456789)))));

        assertThat(() -> parser.parse("node ?0123456789"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node _0123456789"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node a"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node --"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 0x"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 0x_1"), throwsException(KDLParseException.class));
    }

    @Test
    public void test_hexadecimal() {
        KDLNumber kdlNumber = new KDLNumber(new BigDecimal(new BigInteger("0123456789abcdef", 16)), 16);

        assertThat(parser.parse("node 0x0123456789abcdef"), equalTo(doc(node("node", list(kdlNumber)))));
        assertThat(parser.parse("node 0x01234567_89abcdef"), equalTo(doc(node("node", list(kdlNumber)))));
        assertThat(parser.parse("node 0x01234567_89abcdef_"), equalTo(doc(node("node", list(kdlNumber)))));

        assertThat(() -> parser.parse("node 0x_123"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 0xg"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 0xx"), throwsException(KDLParseException.class));
    }

    @Test
    public void test_octal() {
        KDLNumber kdlNumber = new KDLNumber(new BigDecimal(01234567), 8);

        assertThat(parser.parse("node 0o01234567"), equalTo(doc(node("node", list(kdlNumber)))));
        assertThat(parser.parse("node 0o0123_4567"), equalTo(doc(node("node", list(kdlNumber)))));
        assertThat(parser.parse("node 0o01234567_"), equalTo(doc(node("node", list(kdlNumber)))));

        assertThat(() -> parser.parse("node 0o_123"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 0o8"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 0oo"), throwsException(KDLParseException.class));
    }

    @Test
    public void test_binary() {
        KDLNumber kdlNumber = new KDLNumber(new BigDecimal(6), 2);

        assertThat(parser.parse("node 0b0110"), equalTo(doc(node("node", list(kdlNumber)))));
        assertThat(parser.parse("node 0b01_10"), equalTo(doc(node("node", list(kdlNumber)))));
        assertThat(parser.parse("node 0b01___10"), equalTo(doc(node("node", list(kdlNumber)))));
        assertThat(parser.parse("node 0b0110_"), equalTo(doc(node("node", list(kdlNumber)))));

        assertThat(() -> parser.parse("node 0b_0110"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 0b20"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node 0bb"), throwsException(KDLParseException.class));
    }

    @Test
    public void test_raw_string() {
        assertThat(parser.parse("node r\"foo\""), equalTo(doc(node("node", list("foo")))));
        assertThat(parser.parse("node r\"foo\\nbar\""), equalTo(doc(node("node", list("foo\\nbar")))));
        assertThat(parser.parse("node r#\"foo\"#"), equalTo(doc(node("node", list("foo")))));
        assertThat(parser.parse("node r##\"foo\"##"), equalTo(doc(node("node", list("foo")))));
        assertThat(parser.parse("node r\"\\nfoo\\r\""), equalTo(doc(node("node", list("\\nfoo\\r")))));
        assertThat(parser.parse("node r#\"hello\"world\"#"), equalTo(doc(node("node", list("hello\"world")))));

        assertThat(() -> parser.parse("node r##\"foo\"#"), throwsException(KDLParseException.class));
    }

    @Test
    public void test_boolean() {
        assertThat(parser.parse("node true"), equalTo(doc(node("node", list(true)))));
        assertThat(parser.parse("node false"), equalTo(doc(node("node", list(false)))));
    }

    @Test
    public void test_node_space() {
        assertThat(parser.parse("node 1"), equalTo(doc(node("node", list(1)))));
        assertThat(parser.parse("node\t1"), equalTo(doc(node("node", list(1)))));
        assertThat(parser.parse("node\t \\\n 1"), equalTo(doc(node("node", list(1)))));
        assertThat(parser.parse("node\t \\ // hello\n 1"), equalTo(doc(node("node", list(1)))));
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
        assertThat(parser.parse("\\    \t \nfoo"), equalTo(doc(node("foo"))));
        assertThat(parser.parse("\\ // test \nfoo"), equalTo(doc(node("foo"))));
        assertThat(parser.parse("\\ // test \n    foo"), equalTo(doc(node("foo"))));
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
                        node("section", list("First section"),
                                node("paragraph", list("This is the first paragraph")),
                                node("paragraph", list("This is the second paragraph"))
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
                equalTo(doc(node("node1", node("node2")), node("node3"))));
    }

    @Test
    public void test_multiline_strings() {
        assertThat(parser.parse("string \"my\nmultiline\nvalue\""),
                equalTo(doc(node("string", list("my\nmultiline\nvalue")))));
    }

    @Test
    public void test_comments() {
        KDLDocument actual = parser.parse(
                "// C style\n" +

                        "/*\n" +
                        "C style multiline\n" +
                        "*/\n" +

                        "tag /*foo=true*/ bar=false\n" +

                        "/*/*\n" +
                        "hello\n" +
                        "*/*/"
        );

        KDLDocument expected = doc(
                node("tag", map("bar", false))
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
                node("title", list("Some title")),
                node("my-node", list(1, 2, 3, 4))
        );

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void test_utf8() {
        assertThat(parser.parse("smile \"😁\""), equalTo(doc(node("smile", list("😁")))));
        assertThat(parser.parse("ノード お名前=\"☜(ﾟヮﾟ☜)\""),
                equalTo(doc(node("ノード", map("お名前", "☜(ﾟヮﾟ☜)")))));
    }

    @Test
    public void test_node_names() {
        assertThat(parser.parse("\"!@#$@$%Q#$%~@!40\" \"1.2.3\" \"!!!!!\"=true"),
                equalTo(doc(node("!@#$@$%Q#$%~@!40", list("1.2.3"), map("!!!!!", true)))));
        assertThat(parser.parse("foo123~!@#$%^&*.:'|?+ \"weeee\""),
                equalTo(doc(node("foo123~!@#$%^&*.:'|?+", list("weeee")))));
    }

    @Test
    public void test_node_type() {
        assertThat(parser.parse("node"), equalTo(doc(node("node", Optional.empty()))));
        assertThat(parser.parse("(type)node"), equalTo(doc(node("node", Optional.of("type")))));
        assertThat(parser.parse("(type)\"node\""), equalTo(doc(node("node", Optional.of("type")))));
        assertThat(parser.parse("(\"type\")node"), equalTo(doc(node("node", Optional.of("type")))));
        assertThat(parser.parse("(\"t\")node"), equalTo(doc(node("node", Optional.of("t")))));
        assertThat(parser.parse("(r\"t\")node"), equalTo(doc(node("node", Optional.of("t")))));
        assertThat(parser.parse("(\"\")node"), equalTo(doc(node("node", Optional.of("")))));
        assertThat(parser.parse("(r\"\")node"), equalTo(doc(node("node", Optional.of("")))));

        assertThat(() -> parser.parse("()node"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("( )node"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("(type)"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("()"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("( type)node"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("(type )node"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("(\ntype)node"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("(type\n)node"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("(type) node"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("(type)\nnode"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("(type)/*whee*/node"), throwsException(KDLParseException.class));
    }

    @Test
    public void test_arg_type() {
        assertThat(parser.parse("node \"val\""), equalTo(doc(node("node", list(KDLString.from("val"))))));
        assertThat(parser.parse("node (type)\"val\""), equalTo(doc(node("node", list(KDLString.from("val", Optional.of("type")))))));
        assertThat(parser.parse("node (type)r\"val\""), equalTo(doc(node("node", list(KDLString.from("val", Optional.of("type")))))));
        assertThat(parser.parse("node (type)10"), equalTo(doc(node("node", list(KDLNumber.from(10, Optional.of("type")))))));
        assertThat(parser.parse("node (\"type\")10"), equalTo(doc(node("node", list(KDLNumber.from(10, Optional.of("type")))))));
        assertThat(parser.parse("node (r\"type\")10"), equalTo(doc(node("node", list(KDLNumber.from(10, Optional.of("type")))))));
        assertThat(parser.parse("node (\"\")10"), equalTo(doc(node("node", list(KDLNumber.from(10, Optional.of("")))))));
        assertThat(parser.parse("node (type)0x10"), equalTo(doc(node("node", list(KDLNumber.from(16, 16, Optional.of("type")))))));
        assertThat(parser.parse("node (type)0o10"), equalTo(doc(node("node", list(KDLNumber.from(8, 8, Optional.of("type")))))));
        assertThat(parser.parse("node (type)0b10"), equalTo(doc(node("node", list(KDLNumber.from(2, 2, Optional.of("type")))))));
        assertThat(parser.parse("node (type)1.0E2"), equalTo(doc(node("node", list(KDLNumber.from(new BigDecimal("1.0E2"), 10, Optional.of("type")))))));
        assertThat(parser.parse("node (type)10.1"), equalTo(doc(node("node", list(KDLNumber.from(new BigDecimal("10.1"), 10, Optional.of("type")))))));
        assertThat(parser.parse("node (type)true"), equalTo(doc(node("node", list(new KDLBoolean(true, Optional.of("type")))))));
        assertThat(parser.parse("node (type)false"), equalTo(doc(node("node", list(new KDLBoolean(false, Optional.of("type")))))));
        assertThat(parser.parse("node (type)null"), equalTo(doc(node("node", list(new KDLNull(Optional.of("type")))))));

        assertThat(() -> parser.parse("node (type)"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node (type) 10"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node ()10"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node (type)bare"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node (type)fare"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node ( )10"), throwsException(KDLParseException.class));
    }

    @Test
    public void test_property_type() {
        assertThat(parser.parse("node key=\"val\""), equalTo(doc(node("node", map("key", KDLString.from("val"))))));
        assertThat(parser.parse("node key=(type)\"val\""), equalTo(doc(node("node", map("key", KDLString.from("val", Optional.of("type")))))));
        assertThat(parser.parse("node key=(\"type\")\"val\""), equalTo(doc(node("node", map("key", KDLString.from("val", Optional.of("type")))))));
        assertThat(parser.parse("node key=(r\"type\")\"val\""), equalTo(doc(node("node", map("key", KDLString.from("val", Optional.of("type")))))));
        assertThat(parser.parse("node key=(\"\")\"val\""), equalTo(doc(node("node", map("key", KDLString.from("val", Optional.of("")))))));
        assertThat(parser.parse("node key=(type)10"), equalTo(doc(node("node", map("key", KDLNumber.from(10, Optional.of("type")))))));
        assertThat(parser.parse("node key=(type)true"), equalTo(doc(node("node", map("key", new KDLBoolean(true, Optional.of("type")))))));
        assertThat(parser.parse("node key=(type)false"), equalTo(doc(node("node", map("key", new KDLBoolean(false, Optional.of("type")))))));
        assertThat(parser.parse("node key=(type)null"), equalTo(doc(node("node", map("key", new KDLNull(Optional.of("type")))))));

        assertThat(() -> parser.parse("node (type)key=10"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node key= (type)10"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node key=(type) 10"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node key=()10"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node key=( )10"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node key=(\n)10"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node key=()\n10"), throwsException(KDLParseException.class));
        assertThat(() -> parser.parse("node key=(type)bare"), throwsException(KDLParseException.class));
    }

    private KDLDocument doc(KDLNode... nodes) {
        List<KDLNode> nodeList = new ArrayList<>();
        Collections.addAll(nodeList, nodes);
        return new KDLDocument(nodeList);
    }

    private KDLNode node(String ident, Optional<String> type, List<Object> args, Map<String, Object> props, KDLNode... nodes) {
        List<KDLValue<?>> argValues = new ArrayList<>();
        for (Object o : args) {
            argValues.add(KDLValue.from(o));
        }
        Map<String, KDLValue<?>> propValues = new HashMap<>();
        for (Map.Entry<String, Object> e : props.entrySet()) {
            propValues.put(e.getKey(), KDLValue.from(e.getValue()));
        }
        Optional<KDLDocument> children = Optional.empty();
        if (nodes.length > 0) {
            children = Optional.of(doc(nodes));
        }
        return new KDLNode(ident, type, propValues, argValues, children);
    }

    private KDLNode node(String ident, List<Object> args, Map<String, Object> props, KDLNode... nodes) {
        return node(ident, Optional.empty(), args, props, nodes);
    }

    private KDLNode node(String ident, List<Object> args, KDLNode... nodes) {
        return node(ident, Optional.empty(), args, nodes);
    }

    private KDLNode node(String ident, Optional<String> type, List<Object> args, KDLNode... nodes) {
        return node(ident, type, args, Collections.emptyMap(), nodes);
    }

    private KDLNode node(String ident, Map<String, Object> props, KDLNode... nodes) {
        return node(ident, Optional.empty(), props, nodes);
    }

    private KDLNode node(String ident, Optional<String> type, Map<String, Object> props, KDLNode... nodes) {
        return node(ident, type, Collections.emptyList(), props, nodes);
    }

    private KDLNode node(String ident, KDLNode... nodes) {
        return node(ident, Optional.empty(), nodes);
    }

    private KDLNode node(String ident, Optional<String> type, KDLNode... nodes) {
        return node(ident, type, Collections.emptyList(), nodes);
    }

    private List<Object> list(Object... values) {
        final ArrayList<Object> kdlValues = new ArrayList<>();
        Collections.addAll(kdlValues, values);

        return kdlValues;
    }

    private Map<String, Object> map(String key, Object value) {
        return Collections.singletonMap(key, value);
    }
}
