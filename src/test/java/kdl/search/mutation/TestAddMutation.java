package kdl.search.mutation;

import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.objects.KDLValue;
import kdl.parse.KDLParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestAddMutation {
    private final KDLParser parser = new KDLParser();

    @Parameterized.Parameters(name = "{0} -> {2}")
    public static List<Object[]> getCases() {
        final ArrayList<Object[]> cases = new ArrayList<>();

        cases.add(new Object[]{"node", AddMutation.builder().build(), "node"});
        cases.add(new Object[]{"node", AddMutation.builder().addArg(KDLValue.from(15)).build(), "node 15"});
        cases.add(new Object[]{"node", AddMutation.builder().addProp("key", KDLValue.from(true)).build(), "node key=true"});
        cases.add(new Object[]{"node", AddMutation.builder().setChild(KDLDocument.empty()).build(), "node {}"});
        cases.add(new Object[]{"node", AddMutation.builder()
                .setChild(KDLDocument.builder()
                        .addNode(KDLNode.builder()
                                .setIdentifier("node2")
                                .build())
                        .build())
                .build(), "node {node2;}"});
        cases.add(new Object[]{"node \"a\"", AddMutation.builder().addArg(KDLValue.from("a")).build(), "node \"a\" \"a\""});
        cases.add(new Object[]{"node \"a\"", AddMutation.builder().addArg(KDLValue.from("b")).build(), "node \"a\" \"b\""});
        cases.add(new Object[]{"node \"b\"", AddMutation.builder().addArg(KDLValue.from("a")).build(), "node \"b\" \"a\""});
        cases.add(new Object[]{"node key=10", AddMutation.builder().addProp("key2", KDLValue.from(15)).build(), "node key=10 key2=15"});
        cases.add(new Object[]{"node key=10", AddMutation.builder().addProp("key", KDLValue.from(15)).build(), "node key=15"});
        cases.add(new Object[]{"node 10 20", AddMutation.builder().addProp("key", KDLValue.from("val")).build(), "node 10 20 key=\"val\""});
        cases.add(new Object[]{"node", AddMutation.builder()
                .setChild(KDLDocument.builder()
                        .addNode(KDLNode.builder().setIdentifier("node2").build())
                        .build())
                .build(), "node {node2;}"});
        cases.add(new Object[]{"node {}", AddMutation.builder()
                .setChild(KDLDocument.builder()
                        .addNode(KDLNode.builder().setIdentifier("node2").build())
                        .build())
                .build(), "node {node2;}"});
        cases.add(new Object[]{"node {node2;}", AddMutation.builder()
                .setChild(KDLDocument.builder()
                        .addNode(KDLNode.builder()
                                .setIdentifier("node3")
                                .build())
                        .build())
                .build(), "node {node2; node3;}"});

        return cases;
    }

    private final String input;
    private final AddMutation mutation;
    private final String expected;

    public TestAddMutation(String input, AddMutation mutation, String expected) {
        this.input = input;
        this.mutation = mutation;
        this.expected = expected;
    }

    @Test
    public void test() {
        final KDLNode inputNode = parser.parse(input).getNodes().get(0);
        final KDLNode expectedNode = parser.parse(expected).getNodes().get(0);

        final Optional<KDLNode> result = mutation.apply(inputNode);
        assertThat(result, equalTo(Optional.of(expectedNode)));
    }
}
