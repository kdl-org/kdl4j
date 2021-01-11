package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestMutate {

    @Test
    public void testSingle() {
        final KDLNode node1 = KDLNode.builder().setIdentifier("a").build();

        final KDLDocument document = KDLDocument.builder()
                .addNode(node1)
                .build();

        final KDLDocument found = document.search()
                .forNodeId("a")
                .mutate(node -> Optional.of(node.toBuilder().addArg(10).build()));

        final KDLDocument expected = KDLDocument.builder()
                .addNode(node1.toBuilder().addArg(10).build())
                .build();

        assertThat(found, equalTo(expected));
    }

    @Test
    public void testRemove() {
        final KDLNode node1 = KDLNode.builder().setIdentifier("a").build();

        final KDLDocument document = KDLDocument.builder()
                .addNode(node1)
                .build();

        final KDLDocument found = document.search()
                .forNodeId("a")
                .mutate(node -> Optional.empty());

        assertThat(found, equalTo(KDLDocument.empty()));
    }

    @Test
    public void testModifyRoot() {
        final KDLNode leafNode = KDLNode.builder().setIdentifier("l").build();
        final KDLNode branchNode = KDLNode.builder().setIdentifier("b")
                .setChild(KDLDocument.builder().addNode(leafNode).build()).build();
        final KDLNode rootNode = KDLNode.builder().setIdentifier("r")
                .setChild(KDLDocument.builder().addNode(branchNode).build()).build();

        final KDLDocument document = KDLDocument.builder()
                .addNode(rootNode)
                .build();

        final KDLDocument found = document.search()
                .forNodeId("r")
                .mutate(node -> Optional.of(node.toBuilder().addArg(10).build()));

        final KDLDocument expected = KDLDocument.builder()
                .addNode(rootNode.toBuilder().addArg(10).build())
                .build();

        assertThat(found, equalTo(expected));
    }

    @Test
    public void testModifyLeaf() {
        final KDLNode leafNode = KDLNode.builder().setIdentifier("l").build();
        final KDLNode branchNode = KDLNode.builder().setIdentifier("b")
                .setChild(KDLDocument.builder().addNode(leafNode).build()).build();
        final KDLNode rootNode = KDLNode.builder().setIdentifier("r")
                .setChild(KDLDocument.builder().addNode(branchNode).build()).build();

        final KDLDocument document = KDLDocument.builder()
                .addNode(rootNode)
                .build();

        final KDLDocument found = document.search()
                .forNodeId("l")
                .mutate(node -> Optional.of(node.toBuilder().addArg(10).build()));

        final KDLDocument expected = KDLDocument.builder()
                .addNode(rootNode.toBuilder()
                        .setChild(KDLDocument.builder()
                                .addNode(branchNode.toBuilder()
                                        .setChild(KDLDocument.builder()
                                                .addNode(leafNode.toBuilder().addArg(10).build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(found, equalTo(expected));
    }
}
