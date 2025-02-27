package dev.kdl.parse;

import dev.kdl.KdlDocument;
import jakarta.annotation.Nonnull;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static dev.kdl.TestUtils.arguments;
import static dev.kdl.TestUtils.document;
import static dev.kdl.TestUtils.kdlBoolean;
import static dev.kdl.TestUtils.kdlNull;
import static dev.kdl.TestUtils.node;
import static dev.kdl.TestUtils.number;
import static dev.kdl.TestUtils.properties;
import static dev.kdl.TestUtils.string;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Kdl2ParserTest {

	static Stream<Arguments> validTestCases() {
		return Stream.of(
			Arguments.of("", document()),
			Arguments.of("node", document(node("node"))),
			Arguments.of("(type) node", document(node("type", "node"))),
			Arguments.of("node arg", document(node("node", arguments("arg")))),
			Arguments.of("node 1", document(node("node", arguments(1)))),
			Arguments.of("node #null", document(node("node", arguments(kdlNull())))),
			Arguments.of("node #false", document(node("node", arguments(false)))),
			Arguments.of("node (argType) arg", document(node("node", arguments(string("argType", "arg"))))),
			Arguments.of("node (argType) 3.5", document(node("node", arguments(number("argType", 3.5))))),
			Arguments.of("node (argType) #null", document(node("node", arguments(kdlNull("argType"))))),
			Arguments.of("node (argType) #true", document(node("node", arguments(kdlBoolean("argType", true))))),
			Arguments.of("node prop=arg", document(node("node", properties("prop", "arg")))),
			Arguments.of("node prop=1", document(node("node", properties("prop", 1)))),
			Arguments.of("node prop=#null", document(node("node", properties("prop", kdlNull())))),
			Arguments.of("node prop=#false", document(node("node", properties("prop", false)))),
			Arguments.of("node prop=(argType) arg", document(node("node", properties("prop", string("argType", "arg"))))),
			Arguments.of("node prop=(argType) 3.5", document(node("node", properties("prop", number("argType", 3.5))))),
			Arguments.of("node prop=(argType) #null", document(node("node", properties("prop", kdlNull("argType"))))),
			Arguments.of("node prop=(argType) #true", document(node("node", properties("prop", kdlBoolean("argType", true))))),
			Arguments.of("node /- arg", document(node("node"))),
			Arguments.of("node /- prop = 1", document(node("node"))),
			Arguments.of("node {}", document(node("node"))),
			Arguments.of("node {\nchild-node\n}", document(node("node", node("child-node")))),
			Arguments.of("node /- {\nchild-node\n}", document(node("node"))),
			Arguments.of("node /- { ignored } {\nchild-node\n}", document(node("node", node("child-node")))),
			Arguments.of("first-node\nsecond-node", document(node("first-node"), node("second-node"))),
			Arguments.of("first-node; second-node", document(node("first-node"), node("second-node"))),
			Arguments.of("first-node {}; second-node", document(node("first-node"), node("second-node"))),
			Arguments.of("node {\nfirst-child; second-child\n}", document(node("node", node("first-child"), node("second-child")))),
			Arguments.of("/- node1 /- 1.0\nnode2", document(node("node2")))
		);
	}

	@ParameterizedTest
	@MethodSource("validTestCases")
	void validParserTest(String input, KdlDocument expectedDocument) throws KdlParseException, IOException {
		var document = PARSER.parse("test.kdl", input);

		assertThat(document).isEqualTo(expectedDocument);
	}

	static Stream<Arguments> errorTestCases() {
		return Stream.of(
			Arguments.of(
				"(type) ",
				"""
					× Missing node name after node type:
					  ╭─[test.kdl:1:8]
					1 │ (type)
					  ·        ┬
					  ·        ╰ string expected
					  ╰─"""
			),
			Arguments.of(
				"() node",
				"""
					× Missing type name in type annotation:
					  ╭─[test.kdl:1:2]
					1 │ () node
					  ·  ┬
					  ·  ╰ string expected
					  ╰─"""
			),
			Arguments.of(
				"(",
				"""
					× Missing type name in type annotation:
					  ╭─[test.kdl:1:2]
					1 │ (
					  ·  ┬
					  ·  ╰ string expected
					  ╰─"""
			),
			Arguments.of(
				"(type node",
				"""
					× Missing closing parentheses in type annotation:
					  ╭─[test.kdl:1:1]
					1 │ (type node
					  · ──┬──
					  ·   ╰ closing parentheses expected
					  ╰─"""
			),
			Arguments.of(
				"node (argType) ",
				"""
					× Missing value after argument type:
					  ╭─[test.kdl:1:16]
					1 │ node (argType)
					  ·                ┬
					  ·                ╰ value expected
					  ╰─"""
			),
			Arguments.of(
				"node (argType) {}",
				"""
					× Missing value after argument type:
					  ╭─[test.kdl:1:16]
					1 │ node (argType) {}
					  ·                ┬
					  ·                ╰ value expected
					  ╰─"""
			),
			Arguments.of(
				"node prop = ",
				"""
					× Missing property value:
					  ╭─[test.kdl:1:13]
					1 │ node prop =
					  ·             ┬
					  ·             ╰ value expected
					  ╰─"""
			),
			Arguments.of(
				"node prop = {}",
				"""
					× Missing property value:
					  ╭─[test.kdl:1:13]
					1 │ node prop = {}
					  ·             ┬
					  ·             ╰ value expected
					  ╰─"""
			),
			Arguments.of(
				"node { child",
				"""
					× Missing closing brace at the end of children list:
					  ╭─[test.kdl:1:13]
					1 │ node { child
					  ·             ┬
					  ·             ╰ closing brace expected
					  ╰─"""
			),
			Arguments.of(
				"node /- { child",
				"""
					× Missing closing brace at the end of children list:
					  ╭─[test.kdl:1:16]
					1 │ node /- { child
					  ·                ┬
					  ·                ╰ closing brace expected
					  ╰─"""
			),
			Arguments.of(
				"node { child } { more-children }",
				"""
					× More than one list of children provided for node:
					  ╭─[test.kdl:1:16]
					1 │ node { child } { more-children }
					  ·                ┬
					  ·                ╰ second children list
					  ╰─"""
			),
			Arguments.of(
				"first-node {} second-node",
				"""
					× Semi-colon expected between nodes on the same line:
					  ╭─[test.kdl:1:15]
					1 │ first-node {} second-node
					  ·               ┬
					  ·               ╰ semi-colon expected
					  ╰─
					help: nodes need to be separated by a semi-colon or a newline character"""
			),
			Arguments.of(
				"node {\n    first-child {} second-child\n}",
				"""
					× Semi-colon expected between nodes on the same line:
					  ╭─[test.kdl:2:20]
					2 │     first-child {} second-child
					  ·                    ┬
					  ·                    ╰ semi-colon expected
					  ╰─
					help: nodes need to be separated by a semi-colon or a newline character"""
			),
			Arguments.of(
				"node {\n    child1\n    /-\n}",
				"""
					× Valid node expected after slashdash:
					  ╭─[test.kdl:4:1]
					3 │     /-
					4 │ }
					  · ┬
					  · ╰ node expected
					  ╰─"""
			)
		);
	}

	@ParameterizedTest
	@MethodSource("errorTestCases")
	void errorParserTest(@Nonnull String input, @Nonnull String report) {
		assertThatThrownBy(() -> PARSER.parse("test.kdl", input))
			.asInstanceOf(InstanceOfAssertFactories.type(KdlParseException.class))
			.satisfies(exception -> assertThat(Reporter.getReport(exception)).isEqualTo(report));
	}

	private static final Kdl2Parser PARSER = new Kdl2Parser();

}
