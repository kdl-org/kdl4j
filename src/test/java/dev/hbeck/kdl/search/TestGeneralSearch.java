package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.parse.KDLParser;
import dev.hbeck.kdl.search.mutation.Mutation;
import dev.hbeck.kdl.search.predicates.NodePredicate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static dev.hbeck.kdl.search.NodeIDMatcher.hasId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TestGeneralSearch {
    private static final KDLParser parser = new KDLParser();

    @Mock
    public Mutation mutation;

    @Mock
    public NodePredicate predicate;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testMutateEmpty() {
        final KDLDocument inputDoc = KDLDocument.empty();
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();

        assertThat(search.mutate(inputDoc, mutation), equalTo(inputDoc));
        verifyNoInteractions(mutation);
    }

    @Test
    public void testMutateEmptyWithPredicate() {
        final KDLDocument inputDoc = KDLDocument.empty();
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();

        assertThat(search.mutate(inputDoc, mutation), equalTo(inputDoc));
        verifyNoInteractions(mutation);
    }

    @Test
    public void testMutateNothingMatchesPredicate() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node4;}; node3");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);

        assertThat(search.mutate(inputDoc, mutation), equalTo(inputDoc));
        verifyNoInteractions(mutation);
    }

    @Test
    public void testMutateRootNodeMatchesPredicateDelete() {
        final KDLDocument inputDoc = parser.parse("node1; node2; node3");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node2")))).thenReturn(true);
        when(mutation.apply(any())).thenReturn(Optional.empty());

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node3")));
        verify(mutation, times(1)).apply(argThat(hasId("node2")));
    }

    @Test
    public void testMutateBranchNodeMatchesPredicateDelete() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node4 {node5;};}; node3");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
        when(mutation.apply(any())).thenReturn(Optional.empty());

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node2; node3")));
        verify(mutation, times(1)).apply(argThat(hasId("node4")));
    }

    @Test
    public void testMutateLeafNodeMatchesPredicateDelete() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node4;}; node3");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
        when(mutation.apply(any())).thenReturn(Optional.empty());

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node2; node3")));
        verify(mutation, times(1)).apply(argThat(hasId("node4")));
    }

    @Test
    public void testMutateEverythingMatchesPredicate() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node4 {node5;};}; node3");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            return Optional.of(node.toBuilder().addArg(1).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1 1; node2 1 {node4 1 {node5 1;};}; node3 1")));
        verify(mutation, times(5)).apply(any());
        verifyNoMoreInteractions(mutation);
    }

    @Test
    public void testMutateRootNodeMatchesPredicate() {
        final KDLDocument inputDoc = parser.parse("node1; node2; node3");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node2")))).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            return Optional.of(node.toBuilder().addArg(1).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node2 1; node3")));
        verify(mutation, times(1)).apply(argThat(hasId("node2")));
    }

    @Test
    public void testMutateBranchNodeMatchesPredicate() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node4 {node5;};}; node3");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            return Optional.of(node.toBuilder().addArg(1).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node2 {node4 1 {node5;};}; node3")));
        verify(mutation, times(1)).apply(argThat(hasId("node4")));
    }

    @Test
    public void testMutateLeafNodeMatchesPredicate() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node4;}; node3");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            return Optional.of(node.toBuilder().addArg(1).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node2 {node4 1;}; node3")));
        verify(mutation, times(1)).apply(argThat(hasId("node4")));
    }

    @Test
    public void testMutateEverythingMatchesPredicateAddChild() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node4 {node5;};}; node3");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            final KDLDocument.Builder docBuilder = KDLDocument.builder();
            node.getChild().ifPresent(ch -> docBuilder.addNodes(ch.getNodes()));
            docBuilder.addNode(KDLNode.builder().setIdentifier("added").build());

            return Optional.of(node.toBuilder().setChild(docBuilder.build()).build());
        });

        assertThat(search.mutate(inputDoc, mutation),
                equalTo(parser.parse("node1 {added;}; node2 {node4 {node5 {added;}; added;}; added;}; node3 {added;}")));
        verify(mutation, times(5)).apply(any());
        verifyNoMoreInteractions(mutation);
    }

    @Test
    public void testMutateRootNodeMatchesPredicateAddChild() {
        final KDLDocument inputDoc = parser.parse("node1; node2; node3");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node2")))).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            final KDLDocument.Builder docBuilder = KDLDocument.builder();
            node.getChild().ifPresent(ch -> docBuilder.addNodes(ch.getNodes()));
            docBuilder.addNode(KDLNode.builder().setIdentifier("added").build());

            return Optional.of(node.toBuilder().setChild(docBuilder.build()).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node2 {added;}; node3")));
        verify(mutation, times(1)).apply(argThat(hasId("node2")));
    }

    @Test
    public void testMutateBranchNodeMatchesPredicateAddChild() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node4 {node5;};}; node3");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            final KDLDocument.Builder docBuilder = KDLDocument.builder();
            node.getChild().ifPresent(ch -> docBuilder.addNodes(ch.getNodes()));
            docBuilder.addNode(KDLNode.builder().setIdentifier("added").build());

            return Optional.of(node.toBuilder().setChild(docBuilder.build()).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node2 {node4 {node5; added;};}; node3")));
        verify(mutation, times(1)).apply(argThat(hasId("node4")));
    }

    @Test
    public void testMutateLeafNodeMatchesPredicateAddChild() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node4;}; node3");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            final KDLDocument.Builder docBuilder = KDLDocument.builder();
            node.getChild().ifPresent(ch -> docBuilder.addNodes(ch.getNodes()));
            docBuilder.addNode(KDLNode.builder().setIdentifier("added").build());

            return Optional.of(node.toBuilder().setChild(docBuilder.build()).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node2 {node4 {added;};}; node3")));
        verify(mutation, times(1)).apply(argThat(hasId("node4")));
    }

    @Test
    public void testMutateMinDepthAddChild() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node4;}; node3");
        final GeneralSearch search = GeneralSearch.builder()
                .setPredicate(predicate)
                .setMaxDepth(0)
                .build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            final KDLDocument.Builder docBuilder = KDLDocument.builder();
            node.getChild().ifPresent(ch -> docBuilder.addNodes(ch.getNodes()));
            docBuilder.addNode(KDLNode.builder().setIdentifier("added").build());

            return Optional.of(node.toBuilder().setChild(docBuilder.build()).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(inputDoc));
        verify(mutation, never()).apply(any());
    }

    @Test
    public void testMutateEverythingMatchesButDepth() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node4 {node5;};}; node3");
        final GeneralSearch search = GeneralSearch.builder()
                .setPredicate(predicate)
                .setMinDepth(1)
                .setMaxDepth(1)
                .build();
        when(predicate.test(any())).thenReturn(true);
        when(mutation.apply(any())).thenAnswer(invocation -> {
            final KDLNode node = invocation.getArgument(0);
            return Optional.of(node.toBuilder().addArg(1).build());
        });

        assertThat(search.mutate(inputDoc, mutation), equalTo(parser.parse("node1; node2 {node4 1 {node5;};}; node3")));
        verify(mutation, times(1)).apply(argThat(hasId("node4")));
        verifyNoMoreInteractions(mutation);
    }

    @Test
    public void testFilterEmpty() {
        final KDLDocument inputDoc = KDLDocument.empty();
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(true);

        assertThat(search.filter(inputDoc, true), equalTo(KDLDocument.empty()));
    }

    @Test
    public void testFilterNothingMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3;}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);

        assertThat(search.filter(inputDoc, true), equalTo(KDLDocument.empty()));
    }

    @Test
    public void testFilterRootNodeMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3;}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node2")))).thenReturn(true);

        assertThat(search.filter(inputDoc, true), equalTo(parser.parse("node2")));
    }

    @Test
    public void testFilterBranchNodeMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node3")))).thenReturn(true);

        assertThat(search.filter(inputDoc, true), equalTo(parser.parse("node2 {node3;}")));
    }

    @Test
    public void testFilterLeafNodeMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node4")))).thenReturn(true);

        assertThat(search.filter(inputDoc, true), equalTo(parser.parse("node2 {node3 {node4;};}")));
    }

    @Test
    public void testFilterMultipleLeavesMatch() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4; node5;};}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
        when(predicate.test(argThat(hasId("node5")))).thenReturn(true);

        assertThat(search.filter(inputDoc, true), equalTo(parser.parse("node2 {node3 {node4; node5;};}")));
    }

    @Test
    public void testFilterEverythingMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(true);

        assertThat(search.filter(inputDoc, true), equalTo(parser.parse("node1; node2 {node3 {node4;};}")));
    }

    @Test
    public void testListEmpty() {
        final KDLDocument inputDoc = KDLDocument.empty();
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(true);

        assertThat(search.list(inputDoc, false), equalTo(KDLDocument.empty()));
    }

    @Test
    public void testListNothingMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);

        assertThat(search.list(inputDoc, false), equalTo(KDLDocument.empty()));
    }

    @Test
    public void testListNothingMatchesTrim() {
        final KDLDocument inputDoc = parser.parse("node1; node2");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);

        assertThat(search.list(inputDoc, true), equalTo(KDLDocument.empty()));
    }

    @Test
    public void testListEverythingMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3;}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(true);

        assertThat(search.list(inputDoc, false), equalTo(parser.parse("node1; node2 {node3;}; node3")));
    }

    @Test
    public void testListEverythingMatchesTrim() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3;}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(true);

        assertThat(search.list(inputDoc, true), equalTo(parser.parse("node1; node2; node3")));
    }

    @Test
    public void testListRootNodeMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node2")))).thenReturn(true);

        assertThat(search.list(inputDoc, false), equalTo(parser.parse("node2 {node3 {node4;};}")));
    }

    @Test
    public void testListRootNodeMatchesTrim() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node2")))).thenReturn(true);

        assertThat(search.list(inputDoc, true), equalTo(parser.parse("node2")));
    }

    @Test
    public void testListBranchNodeMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node3")))).thenReturn(true);

        assertThat(search.list(inputDoc, false), equalTo(parser.parse("node3 {node4;}")));
    }

    @Test
    public void testListBranchNodeMatchesTrim() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node3")))).thenReturn(true);

        assertThat(search.list(inputDoc, true), equalTo(parser.parse("node3")));
    }

    @Test
    public void testListLeafNodeMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node4")))).thenReturn(true);

        assertThat(search.list(inputDoc, false), equalTo(parser.parse("node4")));
    }

    @Test
    public void testListLeafNodeMatchesTrim() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node4")))).thenReturn(true);

        assertThat(search.list(inputDoc, true), equalTo(parser.parse("node4")));
    }

    @Test
    public void testAnyMatchEmpty() {
        final KDLDocument inputDoc = KDLDocument.empty();
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(true);

        assertFalse(search.anyMatch(inputDoc));
        verifyNoInteractions(predicate);
    }

    @Test
    public void testAnyMatchNothingMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);

        assertFalse(search.anyMatch(inputDoc));
        verify(predicate, times(1)).test(argThat(hasId("node1")));
        verify(predicate, times(1)).test(argThat(hasId("node2")));
    }

    @Test
    public void testAnyMatchEverythingMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(true);

        assertTrue(search.anyMatch(inputDoc));
        verify(predicate, never()).test(argThat(hasId("node2")));
    }

    @Test
    public void testAnyMatchRootNodeMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node2")))).thenReturn(true);

        assertTrue(search.anyMatch(inputDoc));
        verify(predicate, times(1)).test(argThat(hasId("node1")));
        verify(predicate, times(1)).test(argThat(hasId("node2")));
        verify(predicate, never()).test(argThat(hasId("node3")));
        verify(predicate, never()).test(argThat(hasId("node4")));
    }

    @Test
    public void testAnyMatchBranchNodeMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node3")))).thenReturn(true);

        assertTrue(search.anyMatch(inputDoc));
        verify(predicate, times(1)).test(argThat(hasId("node1")));
        verify(predicate, times(1)).test(argThat(hasId("node2")));
        verify(predicate, times(1)).test(argThat(hasId("node3")));
        verify(predicate, never()).test(argThat(hasId("node4")));
    }

    @Test
    public void testAnyMatchLeafNodeMatches() {
        final KDLDocument inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
        final GeneralSearch search = GeneralSearch.builder().setPredicate(predicate).build();
        when(predicate.test(any())).thenReturn(false);
        when(predicate.test(argThat(hasId("node4")))).thenReturn(true);

        assertTrue(search.anyMatch(inputDoc));
        verify(predicate, times(1)).test(argThat(hasId("node1")));
        verify(predicate, times(1)).test(argThat(hasId("node2")));
        verify(predicate, times(1)).test(argThat(hasId("node3")));
        verify(predicate, times(1)).test(argThat(hasId("node4")));
    }
}
