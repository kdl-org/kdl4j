package kdl.search.predicate;

import java.util.Optional;
import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.search.Search;
import kdl.search.predicates.ChildPredicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChildPredicateTest {

	@Mock
	public Search search;

	@Test
	public void testMissingChild() {
		var predicate = ChildPredicate.empty();
		var node = KDLNode.builder().setIdentifier("node").build();

		assertThat(predicate.test(node)).isTrue();
	}

	@Test
	public void testEmptyChild() {
		var predicate = ChildPredicate.empty();
		var node = KDLNode.builder().setIdentifier("node").setChild(KDLDocument.empty()).build();

		assertThat(predicate.test(node)).isTrue();
	}

	@Test
	public void testChildNotEmpty() {
		var predicate = ChildPredicate.empty();
		var node = KDLNode.builder().setIdentifier("node")
			.setChild(KDLDocument.builder()
				.addNode(KDLNode.builder().setIdentifier("identifier").build())
				.build())
			.build();

		assertThat(predicate.test(node)).isFalse();
	}

	@Test
	public void testSearchNoChild() {
		var predicate = new ChildPredicate(Optional.of(search));
		var node = KDLNode.builder().setIdentifier("node").build();

		assertThat(predicate.test(node)).isFalse();
		verifyNoInteractions(search);
	}

	@Test
	public void testSearchReturnsNothing() {
		var predicate = new ChildPredicate(Optional.of(search));
		var child = KDLDocument.builder()
			.addNode(KDLNode.builder().setIdentifier("identifier").build())
			.build();
		var node = KDLNode.builder().setIdentifier("node")
			.setChild(child)
			.build();

		when(search.anyMatch(any())).thenReturn(false);

		assertThat(predicate.test(node)).isFalse();
		verify(search, times(1)).anyMatch(eq(child));
		verifyNoMoreInteractions(search);
	}

	@Test
	public void testSearchReturnsSomething() {
		var predicate = new ChildPredicate(Optional.of(search));
		var child = KDLDocument.builder()
			.addNode(KDLNode.builder().setIdentifier("identifier").build())
			.build();
		var node = KDLNode.builder().setIdentifier("node")
			.setChild(child)
			.build();

		when(search.anyMatch(any())).thenReturn(true);

		assertThat(predicate.test(node)).isTrue();
		verify(search, times(1)).anyMatch(eq(child));
		verifyNoMoreInteractions(search);
	}
}
