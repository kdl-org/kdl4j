package kdl.search.mutation;

import java.util.stream.Stream;
import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.objects.KDLValue;
import kdl.parse.KDLParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class AddMutationTest {
	private final KDLParser parser = new KDLParser();

	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("node", AddMutation.builder().build(), "node"),
			Arguments.of("node", AddMutation.builder().addArg(KDLValue.from(15)).build(), "node 15"),
			Arguments.of("node", AddMutation.builder().addProp("key", KDLValue.from(true)).build(), "node key=true"),
			Arguments.of("node", AddMutation.builder().setChild(KDLDocument.empty()).build(), "node {}"),
			Arguments.of("node", AddMutation.builder()
				.setChild(KDLDocument.builder()
					.addNode(KDLNode.builder()
						.setIdentifier("node2")
						.build())
					.build())
				.build(), "node {node2;}"),
			Arguments.of("node \"a\"", AddMutation.builder().addArg(KDLValue.from("a")).build(), "node \"a\" \"a\""),
			Arguments.of("node \"a\"", AddMutation.builder().addArg(KDLValue.from("b")).build(), "node \"a\" \"b\""),
			Arguments.of("node \"b\"", AddMutation.builder().addArg(KDLValue.from("a")).build(), "node \"b\" \"a\""),
			Arguments.of("node key=10", AddMutation.builder().addProp("key2", KDLValue.from(15)).build(), "node key=10 key2=15"),
			Arguments.of("node key=10", AddMutation.builder().addProp("key", KDLValue.from(15)).build(), "node key=15"),
			Arguments.of("node 10 20", AddMutation.builder().addProp("key", KDLValue.from("val")).build(), "node 10 20 key=\"val\""),
			Arguments.of("node", AddMutation.builder()
				.setChild(KDLDocument.builder()
					.addNode(KDLNode.builder().setIdentifier("node2").build())
					.build())
				.build(), "node {node2;}"),
			Arguments.of("node {}", AddMutation.builder()
				.setChild(KDLDocument.builder()
					.addNode(KDLNode.builder().setIdentifier("node2").build())
					.build())
				.build(), "node {node2;}"),
			Arguments.of("node {node2;}", AddMutation.builder()
				.setChild(KDLDocument.builder()
					.addNode(KDLNode.builder()
						.setIdentifier("node3")
						.build())
					.build())
				.build(), "node {node2; node3;}")
		);
	}

	@ParameterizedTest(name = "{0} -> {2}")
	@MethodSource("getCases")
	public void test(String input, AddMutation mutation, String expected) {
		var inputNode = parser.parse(input).getNodes().get(0);
		var expectedNode = parser.parse(expected).getNodes().get(0);

		var result = mutation.apply(inputNode);

		assertThat(result).contains(expectedNode);
	}
}
