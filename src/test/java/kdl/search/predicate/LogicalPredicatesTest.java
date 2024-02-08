package kdl.search.predicate;

import kdl.objects.KDLNode;
import kdl.search.predicates.ConjunctionPredicate;
import kdl.search.predicates.DisjunctionPredicate;
import kdl.search.predicates.NegatedPredicate;
import kdl.search.predicates.NodeContentPredicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LogicalPredicatesTest {
	private final KDLNode node = KDLNode.builder().setIdentifier("identifier").build();


	@Mock
	public NodeContentPredicate predicate1;

	@Mock
	public NodeContentPredicate predicate2;

	@Test
	public void testNegation() {
		final NegatedPredicate predicate = new NegatedPredicate(predicate1);

		when(predicate1.test(any())).thenReturn(false);
		assertThat(predicate.test(node)).isTrue();

		when(predicate1.test(any())).thenReturn(true);
		assertThat(predicate.test(node)).isFalse();
	}

	@Test
	public void testConjunction() {
		final ConjunctionPredicate predicate = new ConjunctionPredicate(predicate1, predicate2);

		when(predicate1.test(any())).thenReturn(false);
		when(predicate2.test(any())).thenReturn(false);
		assertThat(predicate.test(node)).isFalse();

		when(predicate1.test(any())).thenReturn(false);
		when(predicate2.test(any())).thenReturn(true);
		assertThat(predicate.test(node)).isFalse();

		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(false);
		assertThat(predicate.test(node)).isFalse();

		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(true);
		assertThat(predicate.test(node)).isTrue();
	}

	@Test
	public void testDisjunction() {
		final DisjunctionPredicate predicate = new DisjunctionPredicate(predicate1, predicate2);

		when(predicate1.test(any())).thenReturn(false);
		when(predicate2.test(any())).thenReturn(false);
		assertThat(predicate.test(node)).isFalse();

		when(predicate1.test(any())).thenReturn(false);
		when(predicate2.test(any())).thenReturn(true);
		assertThat(predicate.test(node)).isTrue();

		when(predicate1.test(any())).thenReturn(true);
		assertThat(predicate.test(node)).isTrue();

		when(predicate1.test(any())).thenReturn(true);
		assertThat(predicate.test(node)).isTrue();
	}
}
