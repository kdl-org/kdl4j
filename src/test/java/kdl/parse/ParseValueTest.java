package kdl.parse;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import kdl.objects.KDLBoolean;
import kdl.objects.KDLNull;
import kdl.objects.KDLNumber;
import kdl.objects.KDLString;
import kdl.objects.KDLValue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static kdl.parse.ParserTest.testParser;

public class ParseValueTest {
	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("0", Optional.of(KDLNumber.from(0, 10))),
			Arguments.of("10", Optional.of(KDLNumber.from(10, 10))),
			Arguments.of("-10", Optional.of(KDLNumber.from(-10, 10))),
			Arguments.of("+10", Optional.of(KDLNumber.from(10, 10))),
			Arguments.of("\"\"", Optional.of(KDLString.from(""))),
			Arguments.of("\"r\"", Optional.of(KDLString.from("r"))),
			Arguments.of("\"\n\"", Optional.of(KDLString.from("\n"))),
			Arguments.of("\"\\n\"", Optional.of(KDLString.from("\n"))),
			Arguments.of("r\"\"", Optional.of(KDLString.from(""))),
			Arguments.of("r\"\n\"", Optional.of(KDLString.from("\n"))),
			Arguments.of("r\"\\n\"", Optional.of(KDLString.from("\\n"))),
			Arguments.of("true", Optional.of(new KDLBoolean(true))),
			Arguments.of("false", Optional.of(new KDLBoolean(false))),
			Arguments.of("null", Optional.of(new KDLNull())),
			Arguments.of("\"true\"", Optional.of(KDLString.from("true"))),
			Arguments.of("\"false\"", Optional.of(KDLString.from("false"))),
			Arguments.of("\"null\"", Optional.of(KDLString.from("null"))),
			Arguments.of("garbage", Optional.empty())
		);
	}

	@ParameterizedTest(name = "\"{0}\"")
	@MethodSource("getCases")
	void testParseValue(String input, Optional<KDLValue<?>> expectedResult) throws IOException {
		testParser(input, KDLParser::parseValue, expectedResult);
	}
}
