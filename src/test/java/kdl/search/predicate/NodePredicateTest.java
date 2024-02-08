package kdl.search.predicate;

import java.util.function.Predicate;
import kdl.objects.KDLNode;
import kdl.search.predicates.NodeContentPredicate;
import kdl.search.predicates.NodePredicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NodePredicateTest {

	@Mock
	public Predicate<String> idPredicate;

	@Mock
	public NodeContentPredicate contentPredicate;

	@Test
	public void test() {
		final NodePredicate predicate = new NodePredicate(idPredicate, contentPredicate);
		final KDLNode node = KDLNode.builder().setIdentifier("identifier").build();

		when(idPredicate.test(any())).thenReturn(false);
		when(contentPredicate.test(any())).thenReturn(false);
		assertThat(predicate.test(node)).isFalse();

		when(idPredicate.test(any())).thenReturn(true);
		when(contentPredicate.test(any())).thenReturn(false);
		assertThat(predicate.test(node)).isFalse();

		when(idPredicate.test(any())).thenReturn(false);
		when(contentPredicate.test(any())).thenReturn(true);
		assertThat(predicate.test(node)).isFalse();

		when(idPredicate.test(any())).thenReturn(true);
		when(contentPredicate.test(any())).thenReturn(true);
		assertThat(predicate.test(node)).isTrue();
	}
}
