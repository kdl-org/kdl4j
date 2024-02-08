package kdl.search.mutation;

import java.util.Optional;
import java.util.stream.Stream;
import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.objects.KDLValue;
import kdl.parse.KDLParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class SetMutationTest {

	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("node", SetMutation.builder().build(), "node"),
			Arguments.of("node", SetMutation.builder().setIdentifier("new_node").build(), "new_node"),
			Arguments.of("node", SetMutation.builder().addArg(KDLValue.from(15)).build(), "node 15"),
			Arguments.of("node", SetMutation.builder().addProp("key", KDLValue.from(true)).build(), "node key=true"),
			Arguments.of("node", SetMutation.builder().setChild(Optional.of(KDLDocument.empty())).build(), "node {}"),
			Arguments.of("node {node2;}", SetMutation.builder().setChild(Optional.empty()).build(), "node"),
			Arguments.of("node", SetMutation.builder()
				.setChild(Optional.of(KDLDocument.builder()
					.addNode(KDLNode.builder()
						.setIdentifier("node2")
						.build())
					.build()))
				.build(), "node {node2;}"),
			Arguments.of("node \"a\"", SetMutation.builder().addArg(KDLValue.from("a")).build(), "node \"a\""),
			Arguments.of("node \"a\"", SetMutation.builder().addArg(KDLValue.from("b")).build(), "node \"b\""),
			Arguments.of("node \"b\"", SetMutation.builder().addArg(KDLValue.from("a")).build(), "node \"a\""),
			Arguments.of("node key=10", SetMutation.builder().addProp("key2", KDLValue.from(15)).build(), "node key2=15"),
			Arguments.of("node key=10", SetMutation.builder().addProp("key", KDLValue.from(15)).build(), "node key=15"),
			Arguments.of("node 10 20", SetMutation.builder().addProp("key", KDLValue.from("val")).build(), "node 10 20 key=\"val\""),
			Arguments.of("node", SetMutation.builder()
				.setChild(Optional.ofNullable(KDLDocument.builder()
					.addNode(KDLNode.builder().setIdentifier("node2").build())
					.build()))
				.build(), "node {node2;}"),
			Arguments.of("node {}", SetMutation.builder()
				.setChild(Optional.ofNullable(KDLDocument.builder()
					.addNode(KDLNode.builder().setIdentifier("node2").build())
					.build()))
				.build(), "node {node2;}"),
			Arguments.of("node {node2;}", SetMutation.builder()
				.setChild(Optional.ofNullable(KDLDocument.builder()
					.addNode(KDLNode.builder()
						.setIdentifier("node3")
						.build())
					.build()))
				.build(), "node {node3;}")
		);
	}

	@ParameterizedTest(name = "{0} -> {2}")
	@MethodSource("getCases")
	public void test(String input, SetMutation mutation, String expected) {
		var inputNode = parser.parse(input).getNodes().get(0);
		var expectedNode = parser.parse(expected).getNodes().get(0);

		var result = mutation.apply(inputNode);

		assertThat(result).contains(expectedNode);
	}

	private final KDLParser parser = new KDLParser();
}
