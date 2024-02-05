package kdl.search.predicate;

import kdl.objects.KDLNode;
import kdl.objects.KDLValue;
import kdl.search.predicates.ArgPredicate;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestArgPredicate {
    private final ArgPredicate predicate = new ArgPredicate(val -> val.isString() && val.equals(KDLValue.from("arg")));

    @Test
    public void testOneMatches() {
        final KDLNode node = KDLNode.builder().setIdentifier("identifier").addArg("arg").build();
        assertTrue(predicate.test(node));
    }

    @Test
    public void testSomeMatch() {
        final KDLNode node = KDLNode.builder().setIdentifier("identifier").addArg("arg").addArg("val").build();
        assertTrue(predicate.test(node));
    }

    @Test
    public void testMultipleMatch() {
        final KDLNode node = KDLNode.builder().setIdentifier("identifier").addArg("arg").addArg("arg").build();
        assertTrue(predicate.test(node));
    }

    @Test
    public void testNoneMatch() {
        final KDLNode node = KDLNode.builder().setIdentifier("identifier").addArg("val").build();
        assertFalse(predicate.test(node));

    }
}
