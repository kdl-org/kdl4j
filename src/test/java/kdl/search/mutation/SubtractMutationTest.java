package kdl.search.mutation;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import kdl.objects.KDLProperty;
import kdl.objects.KDLValue;
import kdl.parse.KDLParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;


public class SubtractMutationTest {

	public static Stream<Arguments> getCases() {
		return Stream.of(
			Arguments.of("node \"arg\"", SubtractMutation.builder().addArg(eq("arg")).build(), Optional.of("node")),
			Arguments.of("node \"arg\" \"arg\"", SubtractMutation.builder().addArg(eq("arg")).build(), Optional.of("node")),
			Arguments.of("node \"arg1\" \"arg2\"", SubtractMutation.builder().addArg(eq("arg1")).build(), Optional.of("node \"arg2\"")),
			Arguments.of("node1", SubtractMutation.builder().deleteChild().build(), Optional.of("node1")),
			Arguments.of("node1 {}", SubtractMutation.builder().deleteChild().build(), Optional.of("node1")),
			Arguments.of("node {node2;}", SubtractMutation.builder().deleteChild().build(), Optional.of("node")),
			Arguments.of("node", SubtractMutation.builder().emptyChild().build(), Optional.of("node")),
			Arguments.of("node {}", SubtractMutation.builder().emptyChild().build(), Optional.of("node {}")),
			Arguments.of("node {node2;}", SubtractMutation.builder().emptyChild().build(), Optional.of("node {}")),
			Arguments.of("node prop=\"value\"", SubtractMutation.builder().addProp(eq("prop", "value")).build(), Optional.of("node")),
			Arguments.of("node prop=\"value\"", SubtractMutation.builder().addProp(eq("prop1", "value")).build(), Optional.of("node prop=\"value\"")),
			Arguments.of("node prop=\"value\"", SubtractMutation.builder().addProp(eq("prop", "value1")).build(), Optional.of("node prop=\"value\"")),
			Arguments.of("node", SubtractMutation.builder().build(), Optional.empty()),
			Arguments.of("node 10 prop=15 {node2;}", SubtractMutation.builder().build(), Optional.empty())
		);
	}

	@ParameterizedTest(name = "{0} -> {2}")
	@MethodSource("getCases")
	public void test(String input, SubtractMutation mutation, Optional<String> expected) {
		var inputNode = parser.parse(input).getNodes().get(0);
		var expectedNode = expected.map(ex -> parser.parse(ex).getNodes().get(0));

		var result = mutation.apply(inputNode);

		assertThat(result).isEqualTo(expectedNode);
	}

	private static Predicate<KDLProperty> eq(String key, Object val) {
		var kdlValue = KDLValue.from(val);
		return prop -> key.equals(prop.getKey()) && kdlValue.equals(prop.getValue());
	}

	private static Predicate<KDLValue<?>> eq(Object val) {
		var kdlValue = KDLValue.from(val);
		return kdlValue::equals;
	}

	private final KDLParser parser = new KDLParser();
}
