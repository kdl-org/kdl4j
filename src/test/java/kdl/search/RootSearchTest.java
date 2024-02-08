package kdl.search;

import java.util.Optional;
import java.util.stream.Stream;
import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.parse.KDLParser;
import kdl.print.PrintConfig;
import kdl.search.mutation.AddMutation;
import kdl.search.mutation.Mutation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class RootSearchTest {
	private static final KDLParser parser = new KDLParser();

	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("", Optional.of(""), false,
				Optional.of(AddMutation.builder()
					.setChild(KDLDocument.empty())
					.build())),
			Arguments.of("node1; node2; node3", Optional.of("node1; node2; node3"), false,
				Optional.of(AddMutation.builder()
					.setChild(KDLDocument.empty())
					.build())),
			Arguments.of("node1; node2; node3", Optional.of("node1; node2; node3"), false, Optional.of(AddMutation.builder().build())),
			Arguments.of("node1; node2", Optional.of("node1; node2; node3"), false, Optional.of(AddMutation.builder()
				.setChild(KDLDocument.builder()
					.addNode(KDLNode.builder()
						.setIdentifier("node3")
						.build())
					.build())
				.build())),
			Arguments.of("", Optional.of("node1"), false, Optional.of(AddMutation.builder()
				.setChild(KDLDocument.builder()
					.addNode(KDLNode.builder()
						.setIdentifier("node1")
						.build())
					.build())
				.build())),

			Arguments.of("", Optional.of(""), false, Optional.empty()),
			Arguments.of("node1", Optional.of("node1"), false, Optional.empty()),
			Arguments.of("node1 10", Optional.of("node1 10"), false, Optional.empty()),
			Arguments.of("node1; node2", Optional.of("node1; node2"), false, Optional.empty()),
			Arguments.of("node1; node2 {node3;}", Optional.of("node1; node2 {node3;}; node3"), false, Optional.empty()),
			Arguments.of("node1; node2 {node3;}", Optional.of("node1; node2; node3"), true, Optional.empty()),
			Arguments.of("node1 {node2 {node3;};}", Optional.of("node1; node2; node3"), true, Optional.empty()),
			Arguments.of("node1 {node2 {node3;};}", Optional.of("node1 {node2 {node3;};}; node2 {node3;}; node3"), false, Optional.empty())
		);
	}

	@ParameterizedTest(name = "{0} -> {1}")
	@MethodSource("getCases")
	public void test(String input, Optional<String> expectedRaw, boolean trim, Optional<Mutation> mutation) {
		Search search = new RootSearch();

		var inputDoc = parser.parse(input);
		var expected = expectedRaw.map(parser::parse);

		KDLDocument output = null;
		try {
			if (mutation.isPresent()) {
				output = search.mutate(inputDoc, mutation.get());
			} else {
				output = search.list(inputDoc, trim);
			}
			if (expected.isEmpty()) {
				fail("Expected an error, but got: %s", output.toKDLPretty(PrintConfig.PRETTY_DEFAULT));
			}
		} catch (Exception e) {
			if (expected.isPresent()) {
				throw new RuntimeException(e);
			}
		}

		assertThat(Optional.ofNullable(output)).isEqualTo(expected);
	}
}
