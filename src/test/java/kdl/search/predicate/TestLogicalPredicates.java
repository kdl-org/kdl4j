package kdl.search.predicate;

import kdl.objects.KDLNode;
import kdl.search.predicates.ConjunctionPredicate;
import kdl.search.predicates.DisjunctionPredicate;
import kdl.search.predicates.NegatedPredicate;
import kdl.search.predicates.NodeContentPredicate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestLogicalPredicates {
    private final KDLNode node = KDLNode.builder().setIdentifier("identifier").build();


    @Mock
    public NodeContentPredicate predicate1;

    @Mock
    public NodeContentPredicate predicate2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testNegation() {
        final NegatedPredicate predicate = new NegatedPredicate(predicate1);

        when(predicate1.test(any())).thenReturn(false);
        assertTrue(predicate.test(node));

        when(predicate1.test(any())).thenReturn(true);
        assertFalse(predicate.test(node));
    }

    @Test
    public void testConjunction() {
        final ConjunctionPredicate predicate = new ConjunctionPredicate(predicate1, predicate2);

        when(predicate1.test(any())).thenReturn(false);
        when(predicate2.test(any())).thenReturn(false);
        assertFalse(predicate.test(node));

        when(predicate1.test(any())).thenReturn(false);
        when(predicate2.test(any())).thenReturn(true);
        assertFalse(predicate.test(node));

        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(false);
        assertFalse(predicate.test(node));

        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);
        assertTrue(predicate.test(node));
    }

    @Test
    public void testDisjunction() {
        final DisjunctionPredicate predicate = new DisjunctionPredicate(predicate1, predicate2);

        when(predicate1.test(any())).thenReturn(false);
        when(predicate2.test(any())).thenReturn(false);
        assertFalse(predicate.test(node));

        when(predicate1.test(any())).thenReturn(false);
        when(predicate2.test(any())).thenReturn(true);
        assertTrue(predicate.test(node));

        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(false);
        assertTrue(predicate.test(node));

        when(predicate1.test(any())).thenReturn(true);
        when(predicate2.test(any())).thenReturn(true);
        assertTrue(predicate.test(node));
    }
}
