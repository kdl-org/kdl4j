package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestFilter {

    @Test
    public void testFilterAtRoot() {
        final KDLNode node3 = KDLNode.builder().setIdentifier("c").build();
        final KDLNode node2 = KDLNode.builder().setIdentifier("b").build();
        final KDLNode node1 = KDLNode.builder().setIdentifier("a")
                .setChild(KDLDocument.builder().addNode(node2).build())
                .addArg(10)
                .build();

        final KDLDocument document = KDLDocument.builder()
                .addNode(node1)
                .addNode(node3)
                .build();

        final KDLDocument found = document.search()
                .forNodeId("c")
                .filter();

        final KDLDocument expected = KDLDocument.builder()
                .addNode(node3)
                .build();

        assertThat(found, equalTo(expected));
    }

    @Test
    public void testFilterAllFromRoot() {
        final KDLNode node3 = KDLNode.builder().setIdentifier("c").build();
        final KDLNode node2 = KDLNode.builder().setIdentifier("b").build();
        final KDLNode node1 = KDLNode.builder().setIdentifier("a")
                .setChild(KDLDocument.builder().addNode(node2).build())
                .addArg(10)
                .build();

        final KDLDocument document = KDLDocument.builder()
                .addNode(node1)
                .addNode(node3)
                .build();

        final KDLDocument found = document.search()
                .forNodeId("d")
                .filter();

        assertThat(found, equalTo(KDLDocument.empty()));
    }

    @Test
    public void testFilterMidBranch() {
        final KDLNode node3 = KDLNode.builder().setIdentifier("c").build();
        final KDLNode node2 = KDLNode.builder().setIdentifier("b").build();
        final KDLNode node1 = KDLNode.builder().setIdentifier("a")
                .setChild(KDLDocument.builder().addNode(node2).build())
                .build();

        final KDLDocument document = KDLDocument.builder()
                .addNode(node1)
                .addNode(node3)
                .build();

        final KDLDocument found = document.search()
                .forNodeId("a")
                .filter();

        final KDLDocument expected = KDLDocument.builder()
                .addNode(KDLNode.builder().setIdentifier("a").build())
                .build();

        assertThat(found, equalTo(expected));
    }

    @Test
    public void testFilterLeaves() {
        final KDLNode node3 = KDLNode.builder().setIdentifier("c").build();
        final KDLNode node2 = KDLNode.builder().setIdentifier("b").build();
        final KDLNode node1 = KDLNode.builder().setIdentifier("a")
                .setChild(KDLDocument.builder().addNode(node2).build())
                .build();

        final KDLDocument document = KDLDocument.builder()
                .addNode(node1)
                .addNode(node3)
                .build();

        final KDLDocument found = document.search()
                .forNodeId("a")
                .forNodeId("c")
                .filter();

        final KDLDocument expected = KDLDocument.builder()
                .addNode(KDLNode.builder().setIdentifier("a").build())
                .addNode(node3)
                .build();

        assertThat(found, equalTo(expected));
    }
}
