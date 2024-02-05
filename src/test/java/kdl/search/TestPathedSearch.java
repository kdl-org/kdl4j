package kdl.search;

import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.parse.KDLParser;
import kdl.search.mutation.Mutation;
import kdl.search.predicates.NodePredicate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static kdl.search.NodeIDMatcher.hasId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class TestPathedSearch {
    private static final KDLParser parser = new KDLParser();

    @Mock
    public Mutation mutation;

    @Mock
    public NodePredicate predicate1;

    @Mock
    public NodePredicate predicate2;

    @Mock
    public NodePredicate predicate3;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testMutateEmpty() {
        final KDLDocument inputDoc = KDLDocument.empty();
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .build();
        when(predicate1.test(any())).thenReturn(true);

        assertThat(search.mutate(inputDoc, mutation), equalTo(KDLDocument.empty()));
        verifyNoInteractions(mutation, predicate1);
    }

    @Test
    public void testMutateNothingMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3;}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .build();
        when(predicate1.test(any())).thenReturn(false);

        assertThat(search.mutate(inputDoc, mutation), equalTo(inputDoc));
        verifyNoInteractions(mutation);
    }

    @Test
    public void testMutateEverythingAtRootMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3;}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            return Optional.of(node.toBuilder().addArg(1).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1 1; node2 1 {node3;}")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(mutation, times(1)).apply(argThat(hasId("node1")));
        verify(mutation, times(1)).apply(argThat(hasId("node2")));
        verifyNoMoreInteractions(predicate1, mutation);
    }

    @Test
    public void testMutateEverythingAtSecondLevelMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3; node4;}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            return Optional.of(node.toBuilder().addArg(1).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node2 {node3 1; node4 1;}")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(predicate2, times(1)).test(argThat(hasId("node3")));
        verify(predicate2, times(1)).test(argThat(hasId("node4")));
        verify(mutation, times(1)).apply(argThat(hasId("node3")));
        verify(mutation, times(1)).apply(argThat(hasId("node4")));
        verifyNoMoreInteractions(predicate1, predicate2, mutation);
    }

    @Test
    public void testMutateEverythingAtThirdLevelMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node5;}; node4 {node6;};}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .addLevel(predicate3)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);
        when(predicate3.test(any())).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            return Optional.of(node.toBuilder().addArg(1).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node2 {node3 {node5 1;}; node4 {node6 1;};}")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(predicate2, times(1)).test(argThat(hasId("node3")));
        verify(predicate2, times(1)).test(argThat(hasId("node4")));
        verify(predicate3, times(1)).test(argThat(hasId("node5")));
        verify(predicate3, times(1)).test(argThat(hasId("node6")));
        verify(mutation, times(1)).apply(argThat(hasId("node5")));
        verify(mutation, times(1)).apply(argThat(hasId("node6")));
        verifyNoMoreInteractions(predicate1, predicate2, predicate3, mutation);
    }

    @Test
    public void testMutateSomeThingsAtFirstLevelEverythingAtSecondLevelMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3; node4;}; node5 {node6;}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .build();
        when(predicate1.test(any())).thenReturn(false);
        when(predicate1.test(argThat(hasId("node5")))).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            return Optional.of(node.toBuilder().addArg(1).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node2 {node3; node4;}; node5 {node6 1;}")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(predicate1, times(1)).test(argThat(hasId("node5")));
        verify(predicate2, times(1)).test(argThat(hasId("node6")));
        verify(mutation, times(1)).apply(argThat(hasId("node6")));
        verifyNoMoreInteractions(predicate1, predicate2, mutation);
    }

    @Test
    public void testMutateNothingAtFirstLevelEverythingAtSecondLevelMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3; node4;}; node5 {node6;}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .build();
        when(predicate1.test(any())).thenReturn(false);
        when(predicate2.test(any())).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            return Optional.of(node.toBuilder().addArg(1).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node2 {node3; node4;}; node5 {node6;}")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(predicate1, times(1)).test(argThat(hasId("node5")));
        verifyNoMoreInteractions(predicate1, predicate2, mutation);
    }

    @Test
    public void testMutateEverythingAtFirstLevelNothingAtSecondLevelMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3; node4;}; node5 {node6;}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(false);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            return Optional.of(node.toBuilder().addArg(1).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node2 {node3; node4;}; node5 {node6;}")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(predicate1, times(1)).test(argThat(hasId("node5")));
        verify(predicate2, times(1)).test(argThat(hasId("node3")));
        verify(predicate2, times(1)).test(argThat(hasId("node4")));
        verify(predicate2, times(1)).test(argThat(hasId("node6")));
        verifyNoMoreInteractions(predicate1, predicate2, mutation);
    }

    @Test
    public void testListEmpty() {
        final KDLDocument inputDoc = KDLDocument.empty();
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .build();
        when(predicate1.test(any())).thenReturn(true);

        assertThat(search.list(inputDoc, false), equalTo(KDLDocument.empty()));
        verifyNoInteractions(predicate1);
    }

    @Test
    public void testListNothingMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .build();
        when(predicate1.test(any())).thenReturn(false);

        assertThat(search.list(inputDoc, false), equalTo(KDLDocument.empty()));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verifyNoMoreInteractions(predicate1);
    }

    @Test
    public void testListEverythingMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3;}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .build();
        when(predicate1.test(any())).thenReturn(true);

        assertThat(search.list(inputDoc, false), equalTo(parser.parse("node1; node2 {node3;}")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verifyNoMoreInteractions(predicate1);
    }

    @Test
    public void testListEverythingMatchesTrim() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3;}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .build();
        when(predicate1.test(any())).thenReturn(true);

        assertThat(search.list(inputDoc, true), equalTo(parser.parse("node1; node2")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verifyNoMoreInteractions(predicate1);
    }

    @Test
    public void testListBranch() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);

        assertThat(search.list(inputDoc, false), equalTo(parser.parse("node3 {node4;}")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(predicate2, times(1)).test(argThat(hasId("node3")));
        verifyNoMoreInteractions(predicate1, predicate2);
    }

    @Test
    public void testListBranchTrim() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);

        assertThat(search.list(inputDoc, true), equalTo(parser.parse("node3")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(predicate2, times(1)).test(argThat(hasId("node3")));
        verifyNoMoreInteractions(predicate1, predicate2);
    }

    @Test
    public void testListLeaf() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .addLevel(predicate3)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);
        when(predicate3.test(any())).thenReturn(true);

        assertThat(search.list(inputDoc, false), equalTo(parser.parse("node4")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(predicate2, times(1)).test(argThat(hasId("node3")));
        verify(predicate3, times(1)).test(argThat(hasId("node4")));
        verifyNoMoreInteractions(predicate1, predicate2, predicate3);
    }

    @Test
    public void testListLeafTrim() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .addLevel(predicate3)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);
        when(predicate3.test(any())).thenReturn(true);

        assertThat(search.list(inputDoc, true), equalTo(parser.parse("node4")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(predicate2, times(1)).test(argThat(hasId("node3")));
        verify(predicate3, times(1)).test(argThat(hasId("node4")));
        verifyNoMoreInteractions(predicate1, predicate2, predicate3);
    }

    @Test
    public void testAnyMatchEmpty() {
        final KDLDocument inputDoc = KDLDocument.empty();
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .build();
        when(predicate1.test(any())).thenReturn(true);

        assertFalse(search.anyMatch(inputDoc));
        verifyNoInteractions(predicate1);
    }

    @Test
    public void testAnyMatchNothingMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3;};");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .build();
        when(predicate1.test(any())).thenReturn(false);
        when(predicate2.test(any())).thenReturn(false);

        assertFalse(search.anyMatch(inputDoc));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verifyNoMoreInteractions(predicate1, predicate2);
    }

    @Test
    public void testAnyMatchNothingMatchesAtFirstLevel() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3;};");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .build();
        when(predicate1.test(any())).thenReturn(false);
        when(predicate2.test(any())).thenReturn(true);

        assertFalse(search.anyMatch(inputDoc));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verifyNoMoreInteractions(predicate1, predicate2);
    }

    @Test
    public void testAnyMatchRoot() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3;};");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .build();
        when(predicate1.test(any())).thenReturn(true);

        assertTrue(search.anyMatch(inputDoc));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verifyNoMoreInteractions(predicate1);
    }

    @Test
    public void testAnyMatchBranch() {
        final KDLDocument inputDoc = parser.parse("node1 {node2;}; node3 {node4 {node5;};};");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(false);
        when(predicate2.test(argThat(hasId("node4")))).thenReturn(true);

        assertTrue(search.anyMatch(inputDoc));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node3")));
        verify(predicate2, times(1)).test(argThat(hasId("node2")));
        verify(predicate2, times(1)).test(argThat(hasId("node4")));
        verifyNoMoreInteractions(predicate1, predicate2);
    }

    @Test
    public void testAnyMatchLeaf() {
        final KDLDocument inputDoc = parser.parse("node1 {node2;}; node3 {node4 {node5;};};");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .addLevel(predicate3)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);
        when(predicate3.test(any())).thenReturn(true);

        assertTrue(search.anyMatch(inputDoc));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node3")));
        verify(predicate2, times(1)).test(argThat(hasId("node2")));
        verify(predicate2, times(1)).test(argThat(hasId("node4")));
        verify(predicate3, times(1)).test(argThat(hasId("node5")));
        verifyNoMoreInteractions(predicate1, predicate2);
    }

    @Test
    public void testFilterEmpty() {
        final KDLDocument inputDoc = KDLDocument.empty();
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .build();
        when(predicate1.test(any())).thenReturn(true);

        assertThat(search.filter(inputDoc, true), equalTo(KDLDocument.empty()));
        verifyNoInteractions(predicate1);
    }

    @Test
    public void testFilterNothingMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3;}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(false);

        assertThat(search.filter(inputDoc, true), equalTo(KDLDocument.empty()));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(predicate2, times(1)).test(argThat(hasId("node3")));
        verifyNoMoreInteractions(predicate1, predicate2);
    }

    @Test
    public void testFilterEverythingMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3;}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);

        assertThat(search.filter(inputDoc, true), equalTo(parser.parse("node2 {node3;}")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(predicate2, times(1)).test(argThat(hasId("node3")));
        verifyNoMoreInteractions(predicate1, predicate2);
    }

    @Test
    public void testFilterRootMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3;}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .build();
        when(predicate1.test(any())).thenReturn(true);

        assertThat(search.filter(inputDoc, true), equalTo(parser.parse("node1; node2")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verifyNoMoreInteractions(predicate1, predicate2);
    }

    @Test
    public void testFilterBranchMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);

        assertThat(search.filter(inputDoc, true), equalTo(parser.parse("node2 {node3;}")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(predicate2, times(1)).test(argThat(hasId("node3")));
        verifyNoMoreInteractions(predicate1, predicate2);
    }

    @Test
    public void testFilterLeafMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .addLevel(predicate3)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);
        when(predicate3.test(any())).thenReturn(true);

        assertThat(search.filter(inputDoc, true), equalTo(parser.parse("node2 {node3 {node4;};}")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(predicate2, times(1)).test(argThat(hasId("node3")));
        verify(predicate3, times(1)).test(argThat(hasId("node4")));
        verifyNoMoreInteractions(predicate1, predicate2, predicate3);
    }

    @Test
    public void testFilterMultipleLeavesMatch() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}; node5 {node6 {node7;};}");
        final PathedSearch search = PathedSearch.builder()
                .addLevel(predicate1)
                .addLevel(predicate2)
                .addLevel(predicate3)
                .build();
        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);
        when(predicate3.test(any())).thenReturn(true);

        assertThat(search.filter(inputDoc, true), equalTo(parser.parse("node2 {node3 {node4;};}; node5 {node6 {node7;};}")));
        verify(predicate1, times(1)).test(argThat(hasId("node1")));
        verify(predicate1, times(1)).test(argThat(hasId("node2")));
        verify(predicate1, times(1)).test(argThat(hasId("node5")));
        verify(predicate2, times(1)).test(argThat(hasId("node3")));
        verify(predicate2, times(1)).test(argThat(hasId("node6")));
        verify(predicate3, times(1)).test(argThat(hasId("node4")));
        verify(predicate3, times(1)).test(argThat(hasId("node7")));
        verifyNoMoreInteractions(predicate1, predicate2, predicate3);
    }
}
