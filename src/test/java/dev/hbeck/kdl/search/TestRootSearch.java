package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLString;
import dev.hbeck.kdl.parse.KDLParser;
import dev.hbeck.kdl.print.PrintConfig;
import dev.hbeck.kdl.search.mutation.AddMutation;
import dev.hbeck.kdl.search.mutation.Mutation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class TestRootSearch {
    private static final KDLParser parser = new KDLParser();

    @Parameterized.Parameters(name = "{0} -> {1}")
    public static List<Object[]> getCases() {
        final ArrayList<Object[]> cases = new ArrayList<>();

        cases.add(new Object[]{"", Optional.of(""), false,
                Optional.of(AddMutation.builder()
                        .setChild(KDLDocument.empty())
                        .build())});
        cases.add(new Object[]{"node1; node2; node3", Optional.of("node1; node2; node3"), false,
                Optional.of(AddMutation.builder()
                        .setChild(KDLDocument.empty())
                        .build())});
        cases.add(new Object[]{"node1; node2; node3", Optional.of("node1; node2; node3"), false, Optional.of(AddMutation.builder().build())});
        cases.add(new Object[]{"node1; node2", Optional.of("node1; node2; node3"), false, Optional.of(AddMutation.builder()
                .setChild(KDLDocument.builder()
                        .addNode(KDLNode.builder()
                                .setIdentifier("node3")
                                .build())
                        .build())
                .build())});
        cases.add(new Object[]{"", Optional.of("node1"), false, Optional.of(AddMutation.builder()
                .setChild(KDLDocument.builder()
                        .addNode(KDLNode.builder()
                                .setIdentifier("node1")
                                .build())
                        .build())
                .build())});

        cases.add(new Object[]{"node1; node2", Optional.empty(), false, Optional.of(AddMutation.builder()
                .addArg(KDLString.empty())
                .addProp("key", KDLString.empty())
                .setChild(KDLDocument.builder()
                        .addNode(KDLNode.builder()
                                .setIdentifier("node1")
                                .build())
                        .build())
                .build())});
        cases.add(new Object[]{"node1; node2", Optional.empty(), false, Optional.of(AddMutation.builder()
                .addProp("key", KDLString.empty()))});
        cases.add(new Object[]{"node1; node2", Optional.empty(), false, Optional.of(AddMutation.builder()
                .addArg(KDLString.empty())
                .build())});

        cases.add(new Object[]{"", Optional.of(""), false, Optional.empty()});
        cases.add(new Object[]{"node1", Optional.of("node1"), false, Optional.empty()});
        cases.add(new Object[]{"node1 10", Optional.of("node1 10"), false, Optional.empty()});
        cases.add(new Object[]{"node1; node2", Optional.of("node1; node2"), false, Optional.empty()});
        cases.add(new Object[]{"node1; node2 {node3;}", Optional.of("node1; node2 {node3;}; node3"), false, Optional.empty()});
        cases.add(new Object[]{"node1; node2 {node3;}", Optional.of("node1; node2; node3"), true, Optional.empty()});
        cases.add(new Object[]{"node1 {node2 {node3;};}", Optional.of("node1; node2; node3"), true, Optional.empty()});
        cases.add(new Object[]{"node1 {node2 {node3;};}", Optional.of("node1 {node2 {node3;};}; node2 {node3;}; node3"), false, Optional.empty()});

        return cases;
    }

    private final String input;
    private final Optional<String> expectedRaw;
    private final boolean trim;
    private final Optional<Mutation> mutation;

    public TestRootSearch(String input, Optional<String> expectedRaw, boolean trim, Optional<Mutation> mutation) {
        this.input = input;
        this.expectedRaw = expectedRaw;
        this.trim = trim;
        this.mutation = mutation;
    }

    @Test
    public void test() {
        final Search search = new RootSearch();

        final KDLDocument inputDoc = parser.parse(input);
        final Optional<KDLDocument> expected = expectedRaw.map(parser::parse);

        KDLDocument output = null;
        try {
            if (mutation.isPresent()) {
                output = search.mutate(inputDoc, mutation.get());
            } else {
                output = search.list(inputDoc, trim);
            }
            if (!expected.isPresent()) {
                fail(String.format("Expected an error, but got: %s", output.toKDLPretty(PrintConfig.PRETTY_DEFAULT)));
            }
        } catch (Exception e) {
            if (expected.isPresent()) {
                throw new RuntimeException(e);
            }
        }

        Assert.assertThat(Optional.ofNullable(output), equalTo(expected));
     }
}
