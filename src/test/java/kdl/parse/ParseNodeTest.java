package kdl.parse;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static kdl.parse.ParserTest.testParser;

public class ParseNodeTest {
	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("a", Optional.of(KDLNode.builder().setIdentifier("a").build())),
			Arguments.of("a\n", Optional.of(KDLNode.builder().setIdentifier("a").build())),
			Arguments.of("\"a\"", Optional.of(KDLNode.builder().setIdentifier("a").build())),
			Arguments.of("r\"a\"", Optional.of(KDLNode.builder().setIdentifier("a").build())),
			Arguments.of("r", Optional.of(KDLNode.builder().setIdentifier("r").build())),
			Arguments.of("rrrr", Optional.of(KDLNode.builder().setIdentifier("rrrr").build())),
			Arguments.of("a // stuff", Optional.of(KDLNode.builder().setIdentifier("a").build())),
			Arguments.of("a \"arg\"", Optional.of(KDLNode.builder().setIdentifier("a").addArg("arg").build())),
			Arguments.of("a key=\"val\"", Optional.of(KDLNode.builder().setIdentifier("a").addProp("key", "val").build())),
			Arguments.of("a \"key\"=true", Optional.of(KDLNode.builder().setIdentifier("a").addProp("key", true).build())),
			Arguments.of("a \"arg\" key=\"val\"", Optional.of(KDLNode.builder().setIdentifier("a").addProp("key", "val").addArg("arg").build())),
			Arguments.of("a r#\"arg\"\"# key=\"val\"", Optional.of(KDLNode.builder().setIdentifier("a").addProp("key", "val").addArg("arg\"").build())),
			Arguments.of("a true false null", Optional.of(KDLNode.builder().setIdentifier("a").addArg(true).addArg(false).addNullArg().build())),
			Arguments.of("a /- \"arg1\" \"arg2\"", Optional.of(KDLNode.builder().setIdentifier("a").addArg("arg2").build())),
			Arguments.of("a key=\"val\" key=\"val2\"", Optional.of(KDLNode.builder().setIdentifier("a").addProp("key", "val2").build())),
			Arguments.of("a key=\"val\" /- key=\"val2\"", Optional.of(KDLNode.builder().setIdentifier("a").addProp("key", "val").build())),
			Arguments.of("a {}", Optional.of(KDLNode.builder().setIdentifier("a").setChild(KDLDocument.empty()).build())),
			Arguments.of("a {\nb\n}", Optional.of(KDLNode.builder().setIdentifier("a")
				.setChild(KDLDocument.builder().addNode(KDLNode.builder().setIdentifier("b").build()).build()).build())),
			Arguments.of("a \"arg\" key=null \\\n{\nb\n}", Optional.of(KDLNode.builder().setIdentifier("a").addArg("arg").addNullProp("key")
				.setChild(KDLDocument.builder().addNode(KDLNode.builder().setIdentifier("b").build()).build()).build())),
			Arguments.of("a {\n\n}", Optional.of(KDLNode.builder().setIdentifier("a").setChild(KDLDocument.empty()).build())),
			Arguments.of("a{\n\n}", Optional.of(KDLNode.builder().setIdentifier("a").setChild(KDLDocument.empty()).build())),
			Arguments.of("a\"arg\"", Optional.empty()),
			Arguments.of("a=", Optional.empty()),
			Arguments.of("a /-", Optional.empty())
		);
	}

	@ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
	@MethodSource("getCases")
	public void testParseNode(String input, Optional<KDLNode> expectedResult) throws IOException {
		testParser(input, (parser, context) -> parser.parseNode(context).orElseThrow(), expectedResult);
	}
}
