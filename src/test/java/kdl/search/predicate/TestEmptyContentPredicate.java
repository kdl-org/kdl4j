package kdl.search.predicate;

import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.search.predicates.EmptyContentPredicate;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestEmptyContentPredicate {
    private final EmptyContentPredicate predicate = new EmptyContentPredicate();

    @Test
    public void testShouldMatch() {
        final KDLNode node = KDLNode.builder().setIdentifier("identifier").build();
        assertTrue(predicate.test(node));
    }

    @Test
    public void testHasArgs() {
        final KDLNode node = KDLNode.builder()
                .setIdentifier("identifier")
                .addArg("o")
                .build();

        assertFalse(predicate.test(node));
    }

    @Test
    public void testHasProps() {
        final KDLNode node = KDLNode.builder()
                .setIdentifier("identifier")
                .addProp("key", "val")
                .build();

        assertFalse(predicate.test(node));
    }

    @Test
    public void testHasEmptyChild() {
        final KDLNode node = KDLNode.builder()
                .setIdentifier("identifier")
                .setChild(KDLDocument.empty())
                .build();
        assertFalse(predicate.test(node));
    }

    @Test
    public void testHasChild() {
        final KDLNode node = KDLNode.builder()
                .setIdentifier("identifier")
                .setChild(KDLDocument.builder()
                        .addNode(KDLNode.builder()
                                .setIdentifier("identifier")
                                .build())
                        .build())
                .build();
        assertFalse(predicate.test(node));
    }
}
