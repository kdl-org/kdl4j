package kdl.search.predicate;

import java.util.function.Predicate;
import kdl.objects.KDLNode;
import kdl.objects.KDLValue;
import kdl.search.predicates.PropPredicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PropPredicateTest {
	@Mock
	public Predicate<String> keyPredicate;

	@Mock
	public Predicate<KDLValue<?>> valuePredicate;


	@Test
	public void test() {
		final PropPredicate predicate = new PropPredicate(keyPredicate, valuePredicate);
		final KDLNode node = KDLNode.builder().setIdentifier("identifier")
			.addProp("key", "val")
			.build();

		when(keyPredicate.test(any())).thenReturn(false);
		when(valuePredicate.test(any())).thenReturn(false);
		assertThat(predicate.test(node)).isFalse();

		when(keyPredicate.test(any())).thenReturn(true);
		when(valuePredicate.test(any())).thenReturn(false);
		assertThat(predicate.test(node)).isFalse();

		when(keyPredicate.test(any())).thenReturn(false);
		when(valuePredicate.test(any())).thenReturn(true);
		assertThat(predicate.test(node)).isFalse();

		when(keyPredicate.test(any())).thenReturn(true);
		when(valuePredicate.test(any())).thenReturn(true);
		assertThat(predicate.test(node)).isTrue();
	}
}
