package dev.hbeck.kdl.search.predicate;

import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.search.predicates.NodeContentPredicate;
import dev.hbeck.kdl.search.predicates.NodePredicate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestNodePredicate {

    @Mock
    public Predicate<String> idPredicate;

    @Mock
    public NodeContentPredicate contentPredicate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test() {
        final NodePredicate predicate = new NodePredicate(idPredicate, contentPredicate);
        final KDLNode node = KDLNode.builder().setIdentifier("identifier").build();

        when(idPredicate.test(any())).thenReturn(false);
        when(contentPredicate.test(any())).thenReturn(false);
        assertFalse(predicate.test(node));

        when(idPredicate.test(any())).thenReturn(true);
        when(contentPredicate.test(any())).thenReturn(false);
        assertFalse(predicate.test(node));

        when(idPredicate.test(any())).thenReturn(false);
        when(contentPredicate.test(any())).thenReturn(true);
        assertFalse(predicate.test(node));

        when(idPredicate.test(any())).thenReturn(true);
        when(contentPredicate.test(any())).thenReturn(true);
        assertTrue(predicate.test(node));
    }
}
