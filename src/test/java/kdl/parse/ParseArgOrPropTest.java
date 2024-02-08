package kdl.parse;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static kdl.parse.ParserTest.testParser;

public class ParseArgOrPropTest {
	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("bare", Optional.of("bare")),
			Arguments.of("-10", Optional.of("-10")),
			Arguments.of("r", Optional.of("r")),
			Arguments.of("rrrr", Optional.of("rrrr")),
			Arguments.of("r\"raw\"", Optional.of("raw")),
			Arguments.of("#goals", Optional.of("#goals")),
			Arguments.of("=goals", Optional.empty())
		);
	}

	@ParameterizedTest(name = "\"{0}\"")
	@MethodSource("getCases")
	public void testParseArgOrProp(String input, Optional<String> expectedResult) throws IOException {
		testParser(input, KDLParser::parseIdentifier, expectedResult);
	}
}
