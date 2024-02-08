package kdl.parse;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import kdl.parse.KDLParser.WhitespaceResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static kdl.parse.KDLParser.WhitespaceResult.END_NODE;
import static kdl.parse.KDLParser.WhitespaceResult.NODE_SPACE;
import static kdl.parse.KDLParser.WhitespaceResult.NO_WHITESPACE;
import static kdl.parse.KDLParser.WhitespaceResult.SKIP_NEXT;
import static kdl.parse.ParserTest.testParser;

public class ConsumeWhitespaceAndLinespaceTest {
	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("", Optional.of(END_NODE), ""),
			Arguments.of("\n", Optional.of(END_NODE), ""),
			Arguments.of("   \n/- a", Optional.of(SKIP_NEXT), "a"),
			Arguments.of("\\\r\na", Optional.of(NODE_SPACE), "a"),
			Arguments.of(" \\\r\n \\\n \\\ra", Optional.of(NODE_SPACE), "a"),
			Arguments.of(" a ", Optional.of(NODE_SPACE), "a "),
			Arguments.of("a", Optional.of(NO_WHITESPACE), "a"),
			Arguments.of("\\\na", Optional.of(NODE_SPACE), "a"),
			Arguments.of("\\\ra", Optional.of(NODE_SPACE), "a"),
			Arguments.of("\t", Optional.of(END_NODE), ""),
			Arguments.of("/* comment */a", Optional.of(NODE_SPACE), "a"),
			Arguments.of("\t a", Optional.of(NODE_SPACE), "a"),
			Arguments.of("\\ a", Optional.empty(), "a"),
			Arguments.of("/- ", Optional.empty(), ""),
			Arguments.of("/- \n", Optional.empty(), "\n")
		);
	}

	@ParameterizedTest(name = "\"{0}\"")
	@MethodSource("getCases")
	void testConsumeWhitespaceAndLinespace(String input, Optional<WhitespaceResult> expectedResult, String expectedRemainder) throws IOException {
		testParser(input, KDLParser::consumeWhitespaceAndLinespace, expectedResult, expectedRemainder);
	}
}
