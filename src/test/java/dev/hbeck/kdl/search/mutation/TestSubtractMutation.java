package dev.hbeck.kdl.search.mutation;

import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLProperty;
import dev.hbeck.kdl.objects.KDLValue;
import dev.hbeck.kdl.parse.KDLParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestSubtractMutation {
    private final KDLParser parser = new KDLParser();

    @Parameterized.Parameters(name = "{0} -> {2}")
    public static List<Object[]> getCases() {
        final ArrayList<Object[]> cases = new ArrayList<>();
        
        cases.add(new Object[]{"node \"arg\"", SubtractMutation.builder().addArg(eq("arg")).build(), Optional.of("node")});
        cases.add(new Object[]{"node \"arg\" \"arg\"", SubtractMutation.builder().addArg(eq("arg")).build(), Optional.of("node")});
        cases.add(new Object[]{"node \"arg1\" \"arg2\"", SubtractMutation.builder().addArg(eq("arg1")).build(), Optional.of("node \"arg2\"")});
        cases.add(new Object[]{"node1", SubtractMutation.builder().deleteChild().build(), Optional.of("node1")});
        cases.add(new Object[]{"node1 {}", SubtractMutation.builder().deleteChild().build(), Optional.of("node1")});
        cases.add(new Object[]{"node {node2;}", SubtractMutation.builder().deleteChild().build(), Optional.of("node")});
        cases.add(new Object[]{"node", SubtractMutation.builder().emptyChild().build(), Optional.of("node")});
        cases.add(new Object[]{"node {}", SubtractMutation.builder().emptyChild().build(), Optional.of("node {}")});
        cases.add(new Object[]{"node {node2;}", SubtractMutation.builder().emptyChild().build(), Optional.of("node {}")});
        cases.add(new Object[]{"node prop=\"value\"", SubtractMutation.builder().addProp(eq("prop", "value")).build(), Optional.of("node")});
        cases.add(new Object[]{"node prop=\"value\"", SubtractMutation.builder().addProp(eq("prop1", "value")).build(), Optional.of("node prop=\"value\"")});
        cases.add(new Object[]{"node prop=\"value\"", SubtractMutation.builder().addProp(eq("prop", "value1")).build(), Optional.of("node prop=\"value\"")});

        cases.add(new Object[]{"node", SubtractMutation.builder().build(), Optional.empty()});
        cases.add(new Object[]{"node 10 prop=15 {node2;}", SubtractMutation.builder().build(), Optional.empty()});
        return cases;
    }

    private final String input;
    private final SubtractMutation mutation;
    private final Optional<String> expected;

    public TestSubtractMutation(String input, SubtractMutation mutation, Optional<String> expected) {
        this.input = input;
        this.mutation = mutation;
        this.expected = expected;
    }

    @Test
    public void test() {
        final KDLNode inputNode = parser.parse(input).getNodes().get(0);
        final Optional<KDLNode> expectedNode = expected.map(ex -> parser.parse(ex).getNodes().get(0));

        final Optional<KDLNode> result = mutation.apply(inputNode);
        assertThat(result, equalTo(expectedNode));
    }

    private static Predicate<KDLProperty> eq(String key, Object val) {
        final KDLValue kdlValue = KDLValue.from(val);
        return prop -> key.equals(prop.getKey()) && kdlValue.equals(prop.getValue());
    }

    private static Predicate<KDLValue> eq(Object val) {
        final KDLValue kdlValue = KDLValue.from(val);
        return kdlValue::equals;
    }
}
