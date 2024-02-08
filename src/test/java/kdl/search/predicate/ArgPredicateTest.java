package kdl.search.predicate;

import kdl.objects.KDLNode;
import kdl.objects.KDLValue;
import kdl.search.predicates.ArgPredicate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArgPredicateTest {
	private final ArgPredicate predicate = new ArgPredicate(val -> val.isString() && val.equals(KDLValue.from("arg")));

	@Test
	public void testOneMatches() {
		var node = KDLNode.builder().setIdentifier("identifier").addArg("arg").build();
		assertThat(predicate.test(node)).isTrue();
	}

	@Test
	public void testSomeMatch() {
		var node = KDLNode.builder().setIdentifier("identifier").addArg("arg").addArg("val").build();
		assertThat(predicate.test(node)).isTrue();
	}

	@Test
	public void testMultipleMatch() {
		var node = KDLNode.builder().setIdentifier("identifier").addArg("arg").addArg("arg").build();
		assertThat(predicate.test(node)).isTrue();
	}

	@Test
	public void testNoneMatch() {
		var node = KDLNode.builder().setIdentifier("identifier").addArg("val").build();
		assertThat(predicate.test(node)).isFalse();
	}
}
