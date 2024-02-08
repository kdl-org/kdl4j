package kdl.search.predicate;

import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.search.predicates.EmptyContentPredicate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmptyContentPredicateTest {
	private final EmptyContentPredicate predicate = new EmptyContentPredicate();

	@Test
	public void testShouldMatch() {
		var node = KDLNode.builder().setIdentifier("identifier").build();
		assertThat(predicate.test(node)).isTrue();
	}

	@Test
	public void testHasArgs() {
		var node = KDLNode.builder()
			.setIdentifier("identifier")
			.addArg("o")
			.build();

		assertThat(predicate.test(node)).isFalse();
	}

	@Test
	public void testHasProps() {
		var node = KDLNode.builder()
			.setIdentifier("identifier")
			.addProp("key", "val")
			.build();

		assertThat(predicate.test(node)).isFalse();
	}

	@Test
	public void testHasEmptyChild() {
		var node = KDLNode.builder()
			.setIdentifier("identifier")
			.setChild(KDLDocument.empty())
			.build();
		assertThat(predicate.test(node)).isFalse();
	}

	@Test
	public void testHasChild() {
		var node = KDLNode.builder()
			.setIdentifier("identifier")
			.setChild(KDLDocument.builder()
				.addNode(KDLNode.builder()
					.setIdentifier("identifier")
					.build())
				.build())
			.build();
		assertThat(predicate.test(node)).isFalse();
	}
}
