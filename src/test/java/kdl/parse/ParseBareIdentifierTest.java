package kdl.parse;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static kdl.parse.ParserTest.testParser;

public class ParseBareIdentifierTest {
	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("r", Optional.of("r")),
			Arguments.of("bare", Optional.of("bare")),
			Arguments.of("ぁ", Optional.of("ぁ")),
			Arguments.of("-r", Optional.of("-r")),
			Arguments.of("-1", Optional.of("-1")), //Yes, really. Should it be is another question
			Arguments.of("0hno", Optional.empty()),
			Arguments.of("=no", Optional.empty()),
			Arguments.of("", Optional.empty())
		);
	}

	@ParameterizedTest(name = "\"{0}\"")
	@MethodSource("getCases")
	public void testParseBareIdentifier(String input, Optional<String> expectedResult) throws IOException {
		testParser(input, KDLParser::parseBareIdentifier, expectedResult);
	}
}
