package dev.hbeck.kdl.search.predicate;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.search.Search;
import dev.hbeck.kdl.search.predicates.ChildPredicate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TestChildPredicate {

    @Mock
    public Search search;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testMissingChild() {
        final ChildPredicate predicate = ChildPredicate.empty();
        final KDLNode node = KDLNode.builder().setIdentifier("node").build();

        assertTrue(predicate.test(node));
    }

    @Test
    public void testEmptyChild() {
        final ChildPredicate predicate = ChildPredicate.empty();
        final KDLNode node = KDLNode.builder().setIdentifier("node").setChild(KDLDocument.empty()).build();

        assertTrue(predicate.test(node));
    }

    @Test
    public void testChildNotEmpty() {
        final ChildPredicate predicate = ChildPredicate.empty();
        final KDLNode node = KDLNode.builder().setIdentifier("node")
                .setChild(KDLDocument.builder()
                        .addNode(KDLNode.builder().setIdentifier("identifier").build())
                        .build())
                .build();

        assertFalse(predicate.test(node));
    }

    @Test
    public void testSearchNoChild() {
        final ChildPredicate predicate = new ChildPredicate(Optional.of(search));
        final KDLNode node = KDLNode.builder().setIdentifier("node").build();

        assertFalse(predicate.test(node));
        Mockito.verifyNoInteractions(search);
    }

    @Test
    public void testSearchReturnsNothing() {
        final ChildPredicate predicate = new ChildPredicate(Optional.of(search));
        final KDLDocument child = KDLDocument.builder()
                .addNode(KDLNode.builder().setIdentifier("identifier").build())
                .build();
        final KDLNode node = KDLNode.builder().setIdentifier("node")
                .setChild(child)
                .build();

        when(search.list(any(), anyBoolean())).thenReturn(KDLDocument.empty());

        assertFalse(predicate.test(node));
        verify(search, times(1)).list(eq(child), eq(true));
        verifyNoMoreInteractions(search);
    }

    @Test
    public void testSearchReturnsSomething() {
        final ChildPredicate predicate = new ChildPredicate(Optional.of(search));
        final KDLDocument child = KDLDocument.builder()
                .addNode(KDLNode.builder().setIdentifier("identifier").build())
                .build();
        final KDLNode node = KDLNode.builder().setIdentifier("node")
                .setChild(child)
                .build();

        when(search.list(any(), anyBoolean())).thenReturn(child);

        assertTrue(predicate.test(node));
        verify(search, times(1)).list(eq(child), eq(true));
        verifyNoMoreInteractions(search);
    }
}
