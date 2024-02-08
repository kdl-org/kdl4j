package kdl.parse;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ParserTest {

	static <T> void testParser(String input, ParseFunction<T> parseFunction, Optional<T> expectedResult, Optional<String> expectedRemainder) throws IOException {
        var context = new KDLParseContext(new StringReader(input));

		try {
			var result = parseFunction.parse(parser, context);
			assertThat(expectedResult).contains(result);
		} catch (KDLParseException | KDLInternalException e) {
			if (expectedResult.isPresent()) {
				fail("Unexpected error", e);
			}
		}

		if (expectedRemainder.isPresent()) {
			var remainder = readRemainder(context);
			assertThat(remainder).isEqualTo(expectedRemainder.get());
		}
	}

    private static String readRemainder(KDLParseContext context) {
        final StringBuilder stringBuilder = new StringBuilder();
        try {
            int read = context.read();
            while (read != KDLParser.EOF) {
                stringBuilder.appendCodePoint(read);
                read = context.read();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return stringBuilder.toString();
    }

	static <T> void testParser(String input, ParseFunction<T> parseFunction, Optional<T> expectedResult, String expectedRemainder) throws IOException {
		testParser(input, parseFunction, expectedResult, Optional.ofNullable(expectedRemainder));
	}

	static <T> void testParser(String input, ParseFunction<T> parseFunction, Optional<T> expectedResult) throws IOException {
		testParser(input, parseFunction, expectedResult, Optional.empty());
	}

	private static final KDLParser parser = new KDLParser();

	@FunctionalInterface
	public interface ParseFunction<T> {
		T parse(KDLParser parser, KDLParseContext context) throws IOException;
	}
}
