package kdl.parse;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kdl.objects.KDLBoolean;
import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.objects.KDLNull;
import kdl.objects.KDLNumber;
import kdl.objects.KDLString;
import kdl.objects.KDLValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class KDLParserTest {
	@Test
	public void test_parseEmptyString() {
		assertThat(parser.parse("")).isEqualTo(doc());
		assertThat(parser.parse(" ")).isEqualTo(doc());
		assertThat(parser.parse("\n")).isEqualTo(doc());
	}

	@Test
	public void test_nodes() {
		assertThat(parser.parse("node")).isEqualTo(doc(node("node")));
		assertThat(parser.parse("node\n")).isEqualTo(doc(node("node")));
		assertThat(parser.parse("\nnode\n")).isEqualTo(doc(node("node")));
		assertThat(parser.parse("node1\nnode2")).isEqualTo(doc(node("node1"), node("node2")));
	}

	@Test
	public void test_node() {
		assertThat(parser.parse("node;")).isEqualTo(doc(node("node")));
		assertThat(parser.parse("node 1")).isEqualTo(doc(node("node", list(1))));
		assertThat(parser.parse("node 1 2 \"3\" true false null")).isEqualTo(doc(node("node", list(1, 2, "3", true, false, new KDLNull()))));
		assertThat(parser.parse("node {\n    node2\n}")).isEqualTo(doc(node("node", node("node2"))));
	}

	@Test
	public void test_slashDashComment() {
		assertThat(parser.parse("/-node")).isEqualTo(doc());
		assertThat(parser.parse("/- node")).isEqualTo(doc());
		assertThat(parser.parse("/- node\n")).isEqualTo(doc());
		assertThat(parser.parse("/-node 1 2 3")).isEqualTo(doc());
		assertThat(parser.parse("/-node key=false")).isEqualTo(doc());
		assertThat(parser.parse("/-node{\nnode\n}")).isEqualTo(doc());
		assertThat(parser.parse("/-node 1 2 3 key=\"value\" \\\n{\nnode\n}")).isEqualTo(doc());
	}

	@Test
	public void test_argSlashdashComment() {
		assertThat(parser.parse("node /-1")).isEqualTo(doc(node("node")));
		assertThat(parser.parse("node /-1 2")).isEqualTo(doc(node("node", list(2))));
		assertThat(parser.parse("node 1 /- 2 3")).isEqualTo(doc(node("node", list(1, 3))));
		assertThat(parser.parse("node /--1")).isEqualTo(doc(node("node")));
		assertThat(parser.parse("node /- -1")).isEqualTo(doc(node("node")));
		assertThat(parser.parse("node \\\n/- -1")).isEqualTo(doc(node("node")));
	}

	@Test
	public void test_prop_slashdash_comment() {
		assertThat(parser.parse("node /-key=1")).isEqualTo(doc(node("node")));
		assertThat(parser.parse("node /- key=1")).isEqualTo(doc(node("node")));
		assertThat(parser.parse("node key=1 /-key2=2")).isEqualTo(doc(node("node", map("key", 1))));
	}

	@Test
	public void test_childrenSlashdashComment() {
		assertThat(parser.parse("node /-{}")).isEqualTo(doc(node("node")));
		assertThat(parser.parse("node /- {}")).isEqualTo(doc(node("node")));
		assertThat(parser.parse("node /-{\nnode2\n}")).isEqualTo(doc(node("node")));
	}

	@Test
	public void test_string() {
		assertThat(parser.parse("node \"\"")).isEqualTo(doc(node("node", list(""))));
		assertThat(parser.parse("node \"hello\"")).isEqualTo(doc(node("node", list("hello"))));
		assertThat(parser.parse("node \"hello\\nworld\"")).isEqualTo(doc(node("node", list("hello\nworld"))));
		assertThat(parser.parse("node \"\\u{1F408}\"")).isEqualTo(doc(node("node", list("\uD83D\uDC08"))));
		assertThat(parser.parse("node \"\\\"\\\\\\/\\b\\f\\n\\r\\t\"")).isEqualTo(doc(node("node", list("\"\\/\u0008\u000C\n\r\t"))));
		assertThat(parser.parse("node \"\\u{10}\"")).isEqualTo(doc(node("node", list("\u0010"))));

		assertThatThrownBy(() -> parser.parse("node \"\\i\"")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node \"\\u{c0ffee}\"")).isInstanceOf(KDLParseException.class);
	}

	@Test
	public void test_float() {
		assertThat(parser.parse("node 1.0")).isEqualTo(doc(node("node", list(1.0))));
		assertThat(parser.parse("node 0.0")).isEqualTo(doc(node("node", list(0.0))));
		assertThat(parser.parse("node -1.0")).isEqualTo(doc(node("node", list(-1.0))));
		assertThat(parser.parse("node +1.0")).isEqualTo(doc(node("node", list(1.0))));
		assertThat(parser.parse("node 1.0e10")).isEqualTo(doc(node("node", list(1.0e10))));
		assertThat(parser.parse("node 1.0e-10")).isEqualTo(doc(node("node", list(1.0e-10))));
		assertThat(parser.parse("node 123_456_789.0")).isEqualTo(doc(node("node", list(new BigDecimal("123456789.0")))));
		assertThat(parser.parse("node 123_456_789.0_1")).isEqualTo(doc(node("node", list(new BigDecimal("123456789.01")))));

		assertThatThrownBy(() -> parser.parse("node ?1.0")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node _1.0")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node .0")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 1.0E100E10")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 1.0E1.10")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 1.0.0")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 1.0.0E7")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 1.E7")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 1._0")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 1.")).isInstanceOf(KDLParseException.class);
	}

	@Test
	public void test_integer() {
		assertThat(parser.parse("node 0")).isEqualTo(doc(node("node", list(0))));
		assertThat(parser.parse("node 0123456789")).isEqualTo(doc(node("node", list(123456789))));
		assertThat(parser.parse("node 0123_456_789")).isEqualTo(doc(node("node", list(123456789))));
		assertThat(parser.parse("node 0123_456_789_")).isEqualTo(doc(node("node", list(123456789))));
		assertThat(parser.parse("node +0123456789")).isEqualTo(doc(node("node", list(123456789))));
		assertThat(parser.parse("node -0123456789")).isEqualTo(doc(node("node", list(-123456789))));

		assertThatThrownBy(() -> parser.parse("node ?0123456789")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node _0123456789")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node a")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node --")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 0x")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 0x_1")).isInstanceOf(KDLParseException.class);
	}

	@Test
	public void test_hexadecimal() {
		KDLNumber kdlNumber = new KDLNumber(new BigDecimal(new BigInteger("0123456789abcdef", 16)), 16);

		assertThat(parser.parse("node 0x0123456789abcdef")).isEqualTo(doc(node("node", list(kdlNumber))));
		assertThat(parser.parse("node 0x01234567_89abcdef")).isEqualTo(doc(node("node", list(kdlNumber))));
		assertThat(parser.parse("node 0x01234567_89abcdef_")).isEqualTo(doc(node("node", list(kdlNumber))));

		assertThatThrownBy(() -> parser.parse("node 0x_123")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 0xg")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 0xx")).isInstanceOf(KDLParseException.class);
	}

	@Test
	public void test_octal() {
		KDLNumber kdlNumber = new KDLNumber(new BigDecimal(342391), 8);

		assertThat(parser.parse("node 0o01234567")).isEqualTo(doc(node("node", list(kdlNumber))));
		assertThat(parser.parse("node 0o0123_4567")).isEqualTo(doc(node("node", list(kdlNumber))));
		assertThat(parser.parse("node 0o01234567_")).isEqualTo(doc(node("node", list(kdlNumber))));

		assertThatThrownBy(() -> parser.parse("node 0o_123")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 0o8")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 0oo")).isInstanceOf(KDLParseException.class);
	}

	@Test
	public void test_binary() {
		KDLNumber kdlNumber = new KDLNumber(new BigDecimal(6), 2);

		assertThat(parser.parse("node 0b0110")).isEqualTo(doc(node("node", list(kdlNumber))));
		assertThat(parser.parse("node 0b01_10")).isEqualTo(doc(node("node", list(kdlNumber))));
		assertThat(parser.parse("node 0b01___10")).isEqualTo(doc(node("node", list(kdlNumber))));
		assertThat(parser.parse("node 0b0110_")).isEqualTo(doc(node("node", list(kdlNumber))));

		assertThatThrownBy(() -> parser.parse("node 0b_0110")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 0b20")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node 0bb")).isInstanceOf(KDLParseException.class);
	}

	@Test
	public void test_raw_string() {
		assertThat(parser.parse("node r\"foo\"")).isEqualTo(doc(node("node", list("foo"))));
		assertThat(parser.parse("node r\"foo\\nbar\"")).isEqualTo(doc(node("node", list("foo\\nbar"))));
		assertThat(parser.parse("node r#\"foo\"#")).isEqualTo(doc(node("node", list("foo"))));
		assertThat(parser.parse("node r##\"foo\"##")).isEqualTo(doc(node("node", list("foo"))));
		assertThat(parser.parse("node r\"\\nfoo\\r\"")).isEqualTo(doc(node("node", list("\\nfoo\\r"))));
		assertThat(parser.parse("node r#\"hello\"world\"#")).isEqualTo(doc(node("node", list("hello\"world"))));

		assertThatThrownBy(() -> parser.parse("node r##\"foo\"#")).isInstanceOf(KDLParseException.class);
	}

	@Test
	public void test_boolean() {
		assertThat(parser.parse("node true")).isEqualTo(doc(node("node", list(true))));
		assertThat(parser.parse("node false")).isEqualTo(doc(node("node", list(false))));
	}

	@Test
	public void test_node_space() {
		assertThat(parser.parse("node 1")).isEqualTo(doc(node("node", list(1))));
		assertThat(parser.parse("node\t1")).isEqualTo(doc(node("node", list(1))));
		assertThat(parser.parse("node\t \\\n 1")).isEqualTo(doc(node("node", list(1))));
		assertThat(parser.parse("node\t \\ // hello\n 1")).isEqualTo(doc(node("node", list(1))));
	}

	@Test
	public void test_single_line_comment() {
		assertThat(parser.parse("//hello")).isEqualTo(doc());
		assertThat(parser.parse("// \thello")).isEqualTo(doc());
		assertThat(parser.parse("//hello\n")).isEqualTo(doc());
		assertThat(parser.parse("//hello\r\n")).isEqualTo(doc());
		assertThat(parser.parse("//hello\n\r")).isEqualTo(doc());
		assertThat(parser.parse("//hello\rworld")).isEqualTo(doc(node("world")));
		assertThat(parser.parse("//hello\nworld\r\n")).isEqualTo(doc(node("world")));
	}

	@Test
	public void test_multi_line_comment() {
		assertThat(parser.parse("/*hello*/")).isEqualTo(doc());
		assertThat(parser.parse("/*hello*/\n")).isEqualTo(doc());
		assertThat(parser.parse("/*\nhello\r\n*/")).isEqualTo(doc());
		assertThat(parser.parse("/*\nhello** /\n*/")).isEqualTo(doc());
		assertThat(parser.parse("/**\nhello** /\n*/")).isEqualTo(doc());
		assertThat(parser.parse("/*hello*/world")).isEqualTo(doc(node("world")));
	}

	@Test
	public void test_escline() {
		assertThat(parser.parse("\\\nfoo")).isEqualTo(doc(node("foo")));
		assertThat(parser.parse("\\\n    foo")).isEqualTo(doc(node("foo")));
		assertThat(parser.parse("\\    \t \nfoo")).isEqualTo(doc(node("foo")));
		assertThat(parser.parse("\\ // test \nfoo")).isEqualTo(doc(node("foo")));
		assertThat(parser.parse("\\ // test \n    foo")).isEqualTo(doc(node("foo")));
	}

	@Test
	public void test_whitespace() {
		assertThat(parser.parse(" node")).isEqualTo(doc(node("node")));
		assertThat(parser.parse("\tnode")).isEqualTo(doc(node("node")));
		assertThat(parser.parse("/* \nfoo\r\n */ etc")).isEqualTo(doc(node("etc")));
	}

	@Test
	public void test_newline() {
		assertThat(parser.parse("node1\nnode2")).isEqualTo(doc(node("node1"), node("node2")));
		assertThat(parser.parse("node1\rnode2")).isEqualTo(doc(node("node1"), node("node2")));
		assertThat(parser.parse("node1\r\nnode2")).isEqualTo(doc(node("node1"), node("node2")));
		assertThat(parser.parse("node1\n\nnode2")).isEqualTo(doc(node("node1"), node("node2")));
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

		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void test_semicolon() {
		assertThat(parser.parse("node1; node2; node3")).isEqualTo(doc(node("node1"), node("node2"), node("node3")));
		assertThat(parser.parse("node1 { node2; }; node3")).isEqualTo(doc(node("node1", node("node2")), node("node3")));
	}

	@Test
	public void test_multiline_strings() {
		assertThat(parser.parse("string \"my\nmultiline\nvalue\"")).isEqualTo(doc(node("string", list("my\nmultiline\nvalue"))));
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

		assertThat(actual).isEqualTo(expected);
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

		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void test_utf8() {
		assertThat(parser.parse("smile \"😁\"")).isEqualTo(doc(node("smile", list("😁"))));
		assertThat(parser.parse("ノード お名前=\"☜(ﾟヮﾟ☜)\"")).isEqualTo(doc(node("ノード", map("お名前", "☜(ﾟヮﾟ☜)"))));
	}

	@Test
	public void test_node_names() {
		assertThat(parser.parse("\"!@#$@$%Q#$%~@!40\" \"1.2.3\" \"!!!!!\"=true")).isEqualTo(doc(node("!@#$@$%Q#$%~@!40", list("1.2.3"), map("!!!!!", true))));
		assertThat(parser.parse("foo123~!@#$%^&*.:'|?+ \"weeee\"")).isEqualTo(doc(node("foo123~!@#$%^&*.:'|?+", list("weeee"))));
	}

	@Test
	public void test_node_type() {
		assertThat(parser.parse("node")).isEqualTo(doc(node("node", Optional.empty())));
		assertThat(parser.parse("(type)node")).isEqualTo(doc(node("node", Optional.of("type"))));
		assertThat(parser.parse("(type)\"node\"")).isEqualTo(doc(node("node", Optional.of("type"))));
		assertThat(parser.parse("(\"type\")node")).isEqualTo(doc(node("node", Optional.of("type"))));
		assertThat(parser.parse("(\"t\")node")).isEqualTo(doc(node("node", Optional.of("t"))));
		assertThat(parser.parse("(r\"t\")node")).isEqualTo(doc(node("node", Optional.of("t"))));
		assertThat(parser.parse("(\"\")node")).isEqualTo(doc(node("node", Optional.of(""))));
		assertThat(parser.parse("(r\"\")node")).isEqualTo(doc(node("node", Optional.of(""))));

		assertThatThrownBy(() -> parser.parse("()node")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("( )node")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("(type)")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("()")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("( type)node")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("(type )node")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("(\ntype)node")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("(type\n)node")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("(type) node")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("(type)\nnode")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("(type)/*whee*/node")).isInstanceOf(KDLParseException.class);
	}

	@Test
	public void test_arg_type() {
		assertThat(parser.parse("node \"val\"")).isEqualTo(doc(node("node", list(KDLString.from("val")))));
		assertThat(parser.parse("node (type)\"val\"")).isEqualTo(doc(node("node", list(KDLString.from("val", Optional.of("type"))))));
		assertThat(parser.parse("node (type)r\"val\"")).isEqualTo(doc(node("node", list(KDLString.from("val", Optional.of("type"))))));
		assertThat(parser.parse("node (type)10")).isEqualTo(doc(node("node", list(KDLNumber.from(10, Optional.of("type"))))));
		assertThat(parser.parse("node (\"type\")10")).isEqualTo(doc(node("node", list(KDLNumber.from(10, Optional.of("type"))))));
		assertThat(parser.parse("node (r\"type\")10")).isEqualTo(doc(node("node", list(KDLNumber.from(10, Optional.of("type"))))));
		assertThat(parser.parse("node (\"\")10")).isEqualTo(doc(node("node", list(KDLNumber.from(10, Optional.of(""))))));
		assertThat(parser.parse("node (type)0x10")).isEqualTo(doc(node("node", list(KDLNumber.from(16, 16, Optional.of("type"))))));
		assertThat(parser.parse("node (type)0o10")).isEqualTo(doc(node("node", list(KDLNumber.from(8, 8, Optional.of("type"))))));
		assertThat(parser.parse("node (type)0b10")).isEqualTo(doc(node("node", list(KDLNumber.from(2, 2, Optional.of("type"))))));
		assertThat(parser.parse("node (type)1.0E2")).isEqualTo(doc(node("node", list(KDLNumber.from(new BigDecimal("1.0E2"), 10, Optional.of("type"))))));
		assertThat(parser.parse("node (type)10.1")).isEqualTo(doc(node("node", list(KDLNumber.from(new BigDecimal("10.1"), 10, Optional.of("type"))))));
		assertThat(parser.parse("node (type)true")).isEqualTo(doc(node("node", list(new KDLBoolean(true, Optional.of("type"))))));
		assertThat(parser.parse("node (type)false")).isEqualTo(doc(node("node", list(new KDLBoolean(false, Optional.of("type"))))));
		assertThat(parser.parse("node (type)null")).isEqualTo(doc(node("node", list(new KDLNull(Optional.of("type"))))));

		assertThatThrownBy(() -> parser.parse("node (type)")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node (type) 10")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node ()10")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node (type)bare")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node (type)fare")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node ( )10")).isInstanceOf(KDLParseException.class);
	}

	@Test
	public void test_property_type() {
		assertThat(parser.parse("node key=\"val\"")).isEqualTo(doc(node("node", map("key", KDLString.from("val")))));
		assertThat(parser.parse("node key=(type)\"val\"")).isEqualTo(doc(node("node", map("key", KDLString.from("val", Optional.of("type"))))));
		assertThat(parser.parse("node key=(\"type\")\"val\"")).isEqualTo(doc(node("node", map("key", KDLString.from("val", Optional.of("type"))))));
		assertThat(parser.parse("node key=(r\"type\")\"val\"")).isEqualTo(doc(node("node", map("key", KDLString.from("val", Optional.of("type"))))));
		assertThat(parser.parse("node key=(\"\")\"val\"")).isEqualTo(doc(node("node", map("key", KDLString.from("val", Optional.of(""))))));
		assertThat(parser.parse("node key=(type)10")).isEqualTo(doc(node("node", map("key", KDLNumber.from(10, Optional.of("type"))))));
		assertThat(parser.parse("node key=(type)true")).isEqualTo(doc(node("node", map("key", new KDLBoolean(true, Optional.of("type"))))));
		assertThat(parser.parse("node key=(type)false")).isEqualTo(doc(node("node", map("key", new KDLBoolean(false, Optional.of("type"))))));
		assertThat(parser.parse("node key=(type)null")).isEqualTo(doc(node("node", map("key", new KDLNull(Optional.of("type"))))));

		assertThatThrownBy(() -> parser.parse("node (type)key=10")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node key= (type)10")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node key=(type) 10")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node key=()10")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node key=( )10")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node key=(\n)10")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node key=()\n10")).isInstanceOf(KDLParseException.class);
		assertThatThrownBy(() -> parser.parse("node key=(type)bare")).isInstanceOf(KDLParseException.class);
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

	private static final KDLParser parser = new KDLParser();
}
