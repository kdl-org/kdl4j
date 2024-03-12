package kdl.search.predicate;

import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.search.Search;
import kdl.search.predicates.ChildPredicate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

        when(search.anyMatch(any())).thenReturn(false);

        assertFalse(predicate.test(node));
        verify(search, times(1)).anyMatch(eq(child));
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

        when(search.anyMatch(any())).thenReturn(true);

        assertTrue(predicate.test(node));
        verify(search, times(1)).anyMatch(eq(child));
        verifyNoMoreInteractions(search);
    }
}
