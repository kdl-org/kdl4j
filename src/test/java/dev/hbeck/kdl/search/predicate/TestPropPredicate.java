package dev.hbeck.kdl.search.predicate;

import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLValue;
import dev.hbeck.kdl.search.predicates.PropPredicate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestPropPredicate {
    @Mock
    public Predicate<String> keyPredicate;

    @Mock
    public Predicate<KDLValue> valuePredicate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test() {
        final PropPredicate predicate = new PropPredicate(keyPredicate, valuePredicate);
        final KDLNode node = KDLNode.builder().setIdentifier("identifier")
                .addProp("key", "val")
                .build();

        when(keyPredicate.test(any())).thenReturn(false);
        when(valuePredicate.test(any())).thenReturn(false);
        assertFalse(predicate.test(node));

        when(keyPredicate.test(any())).thenReturn(true);
        when(valuePredicate.test(any())).thenReturn(false);
        assertFalse(predicate.test(node));

        when(keyPredicate.test(any())).thenReturn(false);
        when(valuePredicate.test(any())).thenReturn(true);
        assertFalse(predicate.test(node));

        when(keyPredicate.test(any())).thenReturn(true);
        when(valuePredicate.test(any())).thenReturn(true);
        assertTrue(predicate.test(node));
    }
}
