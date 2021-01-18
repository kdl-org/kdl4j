package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLValue;
import dev.hbeck.kdl.parse.KDLParser;
import dev.hbeck.kdl.search.mutation.AddMutation;
import dev.hbeck.kdl.search.mutation.Mutation;
import dev.hbeck.kdl.search.mutation.SetMutation;
import dev.hbeck.kdl.search.mutation.SubtractMutation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestGeneralSearch {
    private static final KDLParser parser = new KDLParser();

    @Parameterized.Parameters(name = "{0} -> [{2}, {4}, {6}]")
    public static List<Object[]> getCases() {
        final ArrayList<Object[]> cases = new ArrayList<>();

        // Empty -> Empty
        cases.add(new Object[]{"", GeneralSearch.builder().build(),
                "", true,
                "",
                "", SubtractMutation.builder()
                        .addArg(it -> true)
                        .addProp(prop -> true)
                        .deleteChild().build()
        });
        cases.add(new Object[]{"", GeneralSearch.builder().build(),
                "", true,
                "",
                "", AddMutation.builder()
                        .addArg(KDLValue.from("arg"))
                        .addProp("key", KDLValue.from(10))
                        .setChild(KDLDocument.empty())
                        .build()
        });
        cases.add(new Object[]{"", GeneralSearch.builder().build(),
                "", true,
                "",
                "", AddMutation.builder()
                        .addArg(KDLValue.from("arg"))
                        .addProp("key", KDLValue.from(10))
                        .setChild(KDLDocument.builder()
                                .addNode(KDLNode.builder().setIdentifier("identifier").build())
                                .build())
                        .build()
        });
        cases.add(new Object[]{"", GeneralSearch.builder().build(),
                "", true,
                "",
                "", SetMutation.builder()
                        .addArg(KDLValue.from("arg"))
                        .addProp("key", KDLValue.from("o"))
                        .build()
        });
        cases.add(new Object[]{"", GeneralSearch.builder().build(),
                "", false,
                "",
                "", SubtractMutation.builder()
                        .addArg(it -> true)
                        .addProp(prop -> true)
                        .deleteChild().build()
        });
        cases.add(new Object[]{"", GeneralSearch.builder().build(),
                "", false,
                "",
                "", AddMutation.builder()
                        .addArg(KDLValue.from("arg"))
                        .addProp("key", KDLValue.from(10))
                        .setChild(KDLDocument.empty())
                        .build()
        });
        cases.add(new Object[]{"", GeneralSearch.builder().build(),
                "", false,
                "", SetMutation.builder()
                        .addArg(KDLValue.from("arg"))
                        .addProp("key", KDLValue.from("o"))
                        .build()
        });

        // Addition, no predicate
        cases.add(new Object[]{"node", GeneralSearch.builder().build(),
                "node", true,
                "node",
                "node \"arg\" key=10 {}", AddMutation.builder()
                        .addArg(KDLValue.from("arg"))
                        .addProp("key", KDLValue.from(10))
                        .setChild(KDLDocument.empty())
                        .build()
        });
        cases.add(new Object[]{"node1 {node2;}; node3", GeneralSearch.builder().build(),
                "node1; node2; node3;", true,
                "node1 {node2;}; node3",
                "node1 10 {node2 10;}; node3 10", AddMutation.builder()
                .addArg(KDLValue.from(10))
                .build()
        });
        cases.add(new Object[]{"node1 {node2;}; node3", GeneralSearch.builder().build(),
                "node1 {node2;}; node2; node3;", false,
                "node1 {node2;}; node3",
                "node1 10 {node2 10;}; node3 10", AddMutation.builder()
                .addArg(KDLValue.from(10))
                .build()
        });

        // Addition, predicate
        // Addition, depth, predicate
        // Addition, depth, no predicate
        // Addition, add to child

        // Subtraction, no predicate
        // Subtraction, predicate
        // Subtraction, set child
        
        return cases;
    }

    private final String input;
    private final GeneralSearch search;
    private final String filterOutput;
    private final boolean trim;
    private final String listOutput;
    private final String mutateOutput;
    private final Mutation mutation;

    public TestGeneralSearch(String input, GeneralSearch search, String filterOutput, boolean trim, String listOutput, String mutateOutput, Mutation mutation) {
        this.input = input;
        this.search = search;
        this.filterOutput = filterOutput;
        this.trim = trim;
        this.listOutput = listOutput;
        this.mutateOutput = mutateOutput;
        this.mutation = mutation;
    }

    @Test
    public void test() {
        final KDLDocument inputDoc = parser.parse(input);

        assertThat(search.filter(inputDoc), equalTo(parser.parse(filterOutput)));
        assertThat(search.list(inputDoc, trim), equalTo(parser.parse(listOutput)));
        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse(mutateOutput)));
    }
}
