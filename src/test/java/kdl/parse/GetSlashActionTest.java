package kdl.parse;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import kdl.parse.KDLParser.SlashAction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static kdl.parse.KDLParser.SlashAction.END_NODE;
import static kdl.parse.KDLParser.SlashAction.NOTHING;
import static kdl.parse.KDLParser.SlashAction.SKIP_NEXT;
import static kdl.parse.ParserTest.testParser;

public class GetSlashActionTest {
	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("// stuff\n", Optional.of(END_NODE), "\n"),
			Arguments.of("// stuff \r\n", Optional.of(END_NODE), "\r\n"),
			Arguments.of("/- stuff", Optional.of(SKIP_NEXT), " stuff"),
			Arguments.of("/* comment */", Optional.of(NOTHING), ""),
			Arguments.of("/* comment */", Optional.of(NOTHING), ""),
			Arguments.of("/**/", Optional.of(NOTHING), ""),
			Arguments.of("/*/**/*/", Optional.of(NOTHING), ""),
			Arguments.of("/*   /*  */*/", Optional.of(NOTHING), ""),
			Arguments.of("/* ", Optional.empty(), ""),
			Arguments.of("/? ", Optional.empty(), null)
		);
	}

	@ParameterizedTest(name = "\"{0}\"")
	@MethodSource("getCases")
	void testGetSlashAction(String input, Optional<SlashAction> expectedResult, String expectedRemainder) throws IOException {
		testParser(input, (parser, context) -> parser.getSlashAction(context, false), expectedResult, expectedRemainder);
	}
}
