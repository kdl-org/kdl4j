package kdl.parse;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static kdl.parse.ParserTest.testParser;

public class ParseEscapedStringTest {
	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("\"\"", Optional.of("")),
			Arguments.of("\"a\"", Optional.of("a")),
			Arguments.of("\"a\nb\"", Optional.of("a\nb")),
			Arguments.of("\"\\n\"", Optional.of("\n")),
			Arguments.of("\"\\u{0001}\"", Optional.of("\u0001")),
			Arguments.of("\"ぁ\"", Optional.of("ぁ")),
			Arguments.of("\"\\u{3041}\"", Optional.of("ぁ")),
			Arguments.of("\"", Optional.empty()),
			Arguments.of("", Optional.empty())
		);
	}

	@ParameterizedTest(name = "\"{0}\"")
	@MethodSource("getCases")
	public void testParseEscapedString(String input, Optional<String> expectedResult) throws IOException {
		testParser(input, KDLParser::parseEscapedString, expectedResult);
	}
}
