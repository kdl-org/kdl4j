package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLString;
import dev.hbeck.kdl.objects.KDLValue;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestGeneralSearch {

    @Test
    public void testEmpty() {
        final KDLNode node = KDLNode.builder().setIdentifier("a").build();
        final KDLDocument document = KDLDocument.builder().addNode(node).build();

        final List<KDLNode> found = document.search()
                .listAll();

        assertThat(found, equalTo(Collections.singletonList(node)));
    }

    @Test
    public void testBasic() {
        final KDLNode node1 = KDLNode.builder().setIdentifier("a").build();
        final KDLNode node2 = KDLNode.builder().setIdentifier("b").build();
        final KDLDocument document = KDLDocument.builder()
                .addNode(node1)
                .addNode(node2)
                .build();

        final List<KDLNode> found = document.search()
                .forNodeId("a")
                .listAll();

        assertThat(found, equalTo(Collections.singletonList(node1)));
    }

    @Test
    public void testDepth() {
        final KDLNode expected = KDLNode.builder()
                .setIdentifier("b")
                .setChild(KDLDocument.builder()
                        .addNode(KDLNode.builder()
                                .setIdentifier("c")
                                .build())
                        .build())
                .build();

        final KDLNode node = KDLNode.builder()
                .setIdentifier("a")
                .setChild(KDLDocument.builder()
                        .addNode(expected)
                        .build())
                .build();
        final KDLDocument document = KDLDocument.builder().addNode(node).build();

        final List<KDLNode> found = document.search()
                .setMaxDepth(1)
                .setMinDepth(1)
                .listAll();

        assertThat(found, equalTo(Collections.singletonList(expected)));
    }

    @Test
    public void testPropKey() {
        final KDLNode node1 = KDLNode.builder().setIdentifier("a").addProp("key", "val").build();
        final KDLNode node2 = KDLNode.builder().setIdentifier("b").addProp("blah", 10).build();
        final KDLNode node3 = KDLNode.builder().setIdentifier("c").build();

        final KDLDocument document = KDLDocument.builder()
                .addNode(node1)
                .addNode(node2)
                .addNode(node3)
                .build();

        final List<KDLNode> found = document.search()
                .forProperty("key", GeneralSearch.any())
                .listAll();

        assertThat(found, equalTo(Collections.singletonList(node1)));
    }

    @Test
    public void testPropValue() {
        final KDLNode node1 = KDLNode.builder().setIdentifier("a").addProp("key", "val").build();
        final KDLNode node2 = KDLNode.builder().setIdentifier("b").addProp("val", 10).build();
        final KDLNode node3 = KDLNode.builder().setIdentifier("c").build();

        final KDLDocument document = KDLDocument.builder()
                .addNode(node1)
                .addNode(node2)
                .addNode(node3)
                .build();

        final List<KDLNode> found = document.search()
                .forProperty(GeneralSearch.any(), KDLString.from("val"))
                .listAll();

        assertThat(found, equalTo(Collections.singletonList(node1)));
    }

    @Test
    public void testArgs() {
        final KDLNode node3 = KDLNode.builder().setIdentifier("c").build();
        final KDLNode node2 = KDLNode.builder().setIdentifier("b").addProp("key", "val").build();
        final KDLNode node1 = KDLNode.builder().setIdentifier("a")
                .setChild(KDLDocument.builder().addNode(node2).build())
                .addArg(10)
                .build();

        final KDLDocument document = KDLDocument.builder()
                .addNode(node1)
                .addNode(node3)
                .build();

        final List<KDLNode> found = document.search()
                .forProperty(GeneralSearch.any(), KDLString.from("val"))
                .listAll();

        assertThat(found, equalTo(Collections.singletonList(node2)));
    }

    @Test
    public void testAllProps() {
        final KDLNode node3 = KDLNode.builder().setIdentifier("c")
                .addProp("key", "val")
                .build();
        final KDLNode node2 = KDLNode.builder().setIdentifier("b")
                .addProp("key", "val")
                .addProp("key2", "val2")
                .build();

        final KDLNode node1 = KDLNode.builder().setIdentifier("a")
                .setChild(KDLDocument.builder().addNode(node2).build())
                .addArg(10)
                .build();

        final KDLDocument document = KDLDocument.builder()
                .addNode(node1)
                .addNode(node3)
                .build();

        final List<KDLNode> found = document.search()
                .forProperty("key", KDLString.from("val"))
                .forProperty("key2", KDLValue.from("val2"))
                .matchAllPropPredicates()
                .listAll();

        assertThat(found, equalTo(Collections.singletonList(node2)));
    }

    @Test
    public void testAllArgs() {
        final KDLNode node3 = KDLNode.builder().setIdentifier("c")
                .addArg("val")
                .build();
        final KDLNode node2 = KDLNode.builder().setIdentifier("b")
                .addArg("val")
                .addArg("val2")
                .build();

        final KDLNode node1 = KDLNode.builder().setIdentifier("a")
                .setChild(KDLDocument.builder().addNode(node2).build())
                .addArg(10)
                .build();

        final KDLDocument document = KDLDocument.builder()
                .addNode(node1)
                .addNode(node3)
                .build();

        final List<KDLNode> found = document.search()
                .forArg(KDLValue.from("val"))
                .forArg(KDLValue.from("val2"))
                .matchAllArgPredicates()
                .listAll();

        assertThat(found, equalTo(Collections.singletonList(node2)));
    }
}
