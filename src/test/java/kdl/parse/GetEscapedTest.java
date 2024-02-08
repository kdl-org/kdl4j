package kdl.parse;

import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class GetEscapedTest {
	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("n", '\n'),
			Arguments.of("r", '\r'),
			Arguments.of("t", '\t'),
			Arguments.of("\\", '\\'),
			Arguments.of("/", '/'),
			Arguments.of("\"", '\"'),
			Arguments.of("b", '\b'),
			Arguments.of("f", '\f'),
			Arguments.of("u{1}", '\u0001'),
			Arguments.of("u{01}", '\u0001'),
			Arguments.of("u{001}", '\u0001'),
			Arguments.of("u{001}", '\u0001'),
			Arguments.of("u{0001}", '\u0001'),
			Arguments.of("u{00001}", '\u0001'),
			Arguments.of("u{000001}", '\u0001'),
			Arguments.of("u{10FFFF}", 0x10FFFF),
			Arguments.of("i", -2),
			Arguments.of("ux", -2),
			Arguments.of("u{x}", -2),
			Arguments.of("u{0001", -2),
			Arguments.of("u{AX}", -2),
			Arguments.of("u{}", -2),
			Arguments.of("u{0000001}", -2),
			Arguments.of("u{110000}", -2)
		);
	}

	@ParameterizedTest(name = "\"{0}\"")
	@MethodSource("getCases")
	public void testGetEscaped(String input, int expectedResult) throws IOException {
		var context = new KDLParseContext(new StringReader(input));
		var initial = context.read();

		try {
			var result = parser.getEscaped(initial, context);
			assertThat(result).isEqualTo(expectedResult);
		} catch (KDLParseException e) {
			if (expectedResult > 0) {
				throw new KDLParseException("Expected no errors", e);
			}
		}
	}

	private static final KDLParser parser = new KDLParser();
}
