package dev.hbeck.kdl.search.mutation;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLValue;
import dev.hbeck.kdl.parse.KDLParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestSetMutation {
    private final KDLParser parser = new KDLParser();

    @Parameterized.Parameters(name = "{0} -> {2}")
    public static List<Object[]> getCases() {
        final ArrayList<Object[]> cases = new ArrayList<>();

        cases.add(new Object[]{"node", SetMutation.builder().build(), "node"});
        cases.add(new Object[]{"node", SetMutation.builder().setIdentifier("new_node").build(), "new_node"});
        cases.add(new Object[]{"node", SetMutation.builder().addArg(KDLValue.from(15)).build(), "node 15"});
        cases.add(new Object[]{"node", SetMutation.builder().addProp("key", KDLValue.from(true)).build(), "node key=true"});
        cases.add(new Object[]{"node", SetMutation.builder().setChild(Optional.of(KDLDocument.empty())).build(), "node {}"});
        cases.add(new Object[]{"node {node2;}", SetMutation.builder().setChild(Optional.empty()).build(), "node"});
        cases.add(new Object[]{"node", SetMutation.builder()
                .setChild(Optional.of(KDLDocument.builder()
                        .addNode(KDLNode.builder()
                                .setIdentifier("node2")
                                .build())
                        .build()))
                .build(), "node {node2;}"});
        cases.add(new Object[]{"node \"a\"", SetMutation.builder().addArg(KDLValue.from("a")).build(), "node \"a\""});
        cases.add(new Object[]{"node \"a\"", SetMutation.builder().addArg(KDLValue.from("b")).build(), "node \"b\""});
        cases.add(new Object[]{"node \"b\"", SetMutation.builder().addArg(KDLValue.from("a")).build(), "node \"a\""});
        cases.add(new Object[]{"node key=10", SetMutation.builder().addProp("key2", KDLValue.from(15)).build(), "node key2=15"});
        cases.add(new Object[]{"node key=10", SetMutation.builder().addProp("key", KDLValue.from(15)).build(), "node key=15"});
        cases.add(new Object[]{"node 10 20", SetMutation.builder().addProp("key", KDLValue.from("val")).build(), "node 10 20 key=\"val\""});
        cases.add(new Object[]{"node", SetMutation.builder()
                .setChild(Optional.ofNullable(KDLDocument.builder()
                        .addNode(KDLNode.builder().setIdentifier("node2").build())
                        .build()))
                .build(), "node {node2;}"});
        cases.add(new Object[]{"node {}", SetMutation.builder()
                .setChild(Optional.ofNullable(KDLDocument.builder()
                        .addNode(KDLNode.builder().setIdentifier("node2").build())
                        .build()))
                .build(), "node {node2;}"});
        cases.add(new Object[]{"node {node2;}", SetMutation.builder()
                .setChild(Optional.ofNullable(KDLDocument.builder()
                        .addNode(KDLNode.builder()
                                .setIdentifier("node3")
                                .build())
                        .build()))
                .build(), "node {node3;}"});

        return cases;
    }

    private final String input;
    private final SetMutation mutation;
    private final String expected;

    public TestSetMutation(String input, SetMutation mutation, String expected) {
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
