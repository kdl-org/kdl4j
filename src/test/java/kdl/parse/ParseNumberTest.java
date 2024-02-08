package kdl.parse;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import kdl.objects.KDLNumber;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ParseNumberTest {
	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("0", Optional.of(KDLNumber.from(0, 10))),
			Arguments.of("-0", Optional.of(KDLNumber.from(0, 10))),
			Arguments.of("1", Optional.of(KDLNumber.from(1, 10))),
			Arguments.of("01", Optional.of(KDLNumber.from(1, 10))),
			Arguments.of("10", Optional.of(KDLNumber.from(10, 10))),
			Arguments.of("1_0", Optional.of(KDLNumber.from(10, 10))),
			Arguments.of("-10", Optional.of(KDLNumber.from(-10, 10))),
			Arguments.of("+10", Optional.of(KDLNumber.from(10, 10))),
			Arguments.of("1.0", KDLNumber.from("1.0", 10)),
			Arguments.of("1e10", KDLNumber.from("1e10", 10)),
			Arguments.of("1e+10", KDLNumber.from("1e10", 10)),
			Arguments.of("1E+10", KDLNumber.from("1e10", 10)),
			Arguments.of("1e-10", KDLNumber.from("1e-10", 10)),
			Arguments.of("-1e-10", KDLNumber.from("-1e-10", 10)),
			Arguments.of("+1e-10", KDLNumber.from("1e-10", 10)),
			Arguments.of("0x0", Optional.of(KDLNumber.from(0, 16))),
			Arguments.of("0xFF", Optional.of(KDLNumber.from(255, 16))),
			Arguments.of("0xF_F", Optional.of(KDLNumber.from(255, 16))),
			Arguments.of("-0xFF", Optional.of(KDLNumber.from(-255, 16))),
			Arguments.of("0o0", Optional.of(KDLNumber.from(0, 8))),
			Arguments.of("0o7", Optional.of(KDLNumber.from(7, 8))),
			Arguments.of("0o77", Optional.of(KDLNumber.from(63, 8))),
			Arguments.of("0o7_7", Optional.of(KDLNumber.from(63, 8))),
			Arguments.of("-0o77", Optional.of(KDLNumber.from(-63, 8))),
			Arguments.of("0b0", Optional.of(KDLNumber.from(0, 2))),
			Arguments.of("0b1", Optional.of(KDLNumber.from(1, 2))),
			Arguments.of("0b10", Optional.of(KDLNumber.from(2, 2))),
			Arguments.of("0b1_0", Optional.of(KDLNumber.from(2, 2))),
			Arguments.of("-0b10", Optional.of(KDLNumber.from(-2, 2))),
			Arguments.of("A", Optional.empty()),
			Arguments.of("_", Optional.empty()),
			Arguments.of("_1", Optional.empty()),
			Arguments.of("+_1", Optional.empty()),
			Arguments.of("0xRR", Optional.empty()),
			Arguments.of("0o8", Optional.empty()),
			Arguments.of("0b2", Optional.empty())
		);
	}

	@ParameterizedTest(name = "\"{0}\"")
	@MethodSource("getCases")
	public void testParseNumber(String input, Optional<KDLNumber> expectedResult) throws IOException {
		ParserTest.testParser(input, (parser, context) -> parser.parseNumber(context, Optional.empty()), expectedResult);
	}
}
