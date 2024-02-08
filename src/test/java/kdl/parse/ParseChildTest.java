package kdl.parse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static kdl.parse.ParserTest.testParser;

public class ParseChildTest {
	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("{}", Optional.of(doc())),
			Arguments.of("{\n\n}", Optional.of(doc())),
			Arguments.of("{\na\n}", Optional.of(doc("a"))),
			Arguments.of("{\n\na\n\nb\n}", Optional.of(doc("a", "b"))),
			Arguments.of("{\na\nb\n}", Optional.of(doc("a", "b"))),
			Arguments.of("", Optional.empty()),
			Arguments.of("{", Optional.empty()),
			Arguments.of("{\n", Optional.empty()),
			Arguments.of("{\na /-", Optional.empty()),
			Arguments.of("{\na\n/-", Optional.empty())
		);
	}

	private static KDLDocument doc(String... identifiers) {
		final List<KDLNode> nodes = Arrays.stream(identifiers)
			.map(id -> KDLNode.builder().setIdentifier(id).build())
			.collect(Collectors.toList());
		return new KDLDocument(nodes);
	}

	@ParameterizedTest(name = "\"{0}\"")
	@MethodSource("getCases")
	public void testParseChild(String input, Optional<KDLDocument> expectedResult) throws IOException {
		testParser(input, KDLParser::parseChild, expectedResult);
	}
}
