package kdl.parse;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static kdl.parse.ParserTest.testParser;

public class ParseRawStringTest {
	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("r\"\"", Optional.of(""), ""),
			Arguments.of("r\"\n\"", Optional.of("\n"), ""),
			Arguments.of("r\"\\n\"", Optional.of("\\n"), ""),
			Arguments.of("r\"\\u{0001}\"", Optional.of("\\u{0001}"), ""),
			Arguments.of("r#\"\"#", Optional.of(""), ""),
			Arguments.of("r#\"a\"#", Optional.of("a"), ""),
			Arguments.of("r##\"\"#\"##", Optional.of("\"#"), ""),
			Arguments.of("\"\"", Optional.empty(), "\""),
			Arguments.of("r", Optional.empty(), ""),
			Arguments.of("r\"", Optional.empty(), ""),
			Arguments.of("r#\"a\"##", Optional.empty(), ""),
			Arguments.of("", Optional.empty(), "")
		);
	}

	@ParameterizedTest(name = "\"{0}\"")
	@MethodSource("getCases")
	public void testParseRawString(String input, Optional<String> expectedResult, String expectedRemainder) throws IOException {
		testParser(input, KDLParser::parseRawString, expectedResult, expectedRemainder);
	}
}
