package kdl.search;

import java.util.Optional;
import kdl.objects.KDLDocument;
import kdl.objects.KDLNode;
import kdl.parse.KDLParser;
import kdl.search.mutation.Mutation;
import kdl.search.predicates.NodePredicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static kdl.search.NodeIDMatcher.hasId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PathedSearchTest {
	private static final KDLParser parser = new KDLParser();

	@Mock
	public Mutation mutation;

	@Mock
	public NodePredicate predicate1;

	@Mock
	public NodePredicate predicate2;

	@Mock
	public NodePredicate predicate3;

	@Test
	public void testMutateEmpty() {
		var inputDoc = KDLDocument.empty();
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.build();

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(KDLDocument.empty());
		verifyNoInteractions(mutation, predicate1);
	}

	@Test
	public void testMutateNothingMatches() {
		var inputDoc = parser.parse("node1; node2 {node3;}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.build();
		when(predicate1.test(any())).thenReturn(false);

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(inputDoc);
		verifyNoInteractions(mutation);
	}

	@Test
	public void testMutateEverythingAtRootMatches() {
		var inputDoc = parser.parse("node1; node2 {node3;}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(mutation.apply(any())).thenAnswer(invocation -> {
			KDLNode node = invocation.getArgument(0);
			return Optional.of(node.toBuilder().addArg(1).build());
		});

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1 1; node2 1 {node3;}"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(mutation, times(1)).apply(argThat(hasId("node1")));
		verify(mutation, times(1)).apply(argThat(hasId("node2")));
		verifyNoMoreInteractions(predicate1, mutation);
	}

	@Test
	public void testMutateEverythingAtSecondLevelMatches() {
		var inputDoc = parser.parse("node1; node2 {node3; node4;}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(true);
		when(mutation.apply(any())).thenAnswer(invocation -> {
			KDLNode node = invocation.getArgument(0);
			return Optional.of(node.toBuilder().addArg(1).build());
		});

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node2 {node3 1; node4 1;}"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(predicate2, times(1)).test(argThat(hasId("node3")));
		verify(predicate2, times(1)).test(argThat(hasId("node4")));
		verify(mutation, times(1)).apply(argThat(hasId("node3")));
		verify(mutation, times(1)).apply(argThat(hasId("node4")));
		verifyNoMoreInteractions(predicate1, predicate2, mutation);
	}

	@Test
	public void testMutateEverythingAtThirdLevelMatches() {
		var inputDoc = parser.parse("node1; node2 {node3 {node5;}; node4 {node6;};}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.addLevel(predicate3)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(true);
		when(predicate3.test(any())).thenReturn(true);
		when(mutation.apply(any())).thenAnswer(invocation -> {
			KDLNode node = invocation.getArgument(0);
			return Optional.of(node.toBuilder().addArg(1).build());
		});

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node2 {node3 {node5 1;}; node4 {node6 1;};}"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(predicate2, times(1)).test(argThat(hasId("node3")));
		verify(predicate2, times(1)).test(argThat(hasId("node4")));
		verify(predicate3, times(1)).test(argThat(hasId("node5")));
		verify(predicate3, times(1)).test(argThat(hasId("node6")));
		verify(mutation, times(1)).apply(argThat(hasId("node5")));
		verify(mutation, times(1)).apply(argThat(hasId("node6")));
		verifyNoMoreInteractions(predicate1, predicate2, predicate3, mutation);
	}

	@Test
	public void testMutateSomeThingsAtFirstLevelEverythingAtSecondLevelMatches() {
		var inputDoc = parser.parse("node1; node2 {node3; node4;}; node5 {node6;}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.build();
		when(predicate1.test(any())).thenReturn(false);
		when(predicate1.test(argThat(hasId("node5")))).thenReturn(true);
		when(predicate2.test(any())).thenReturn(true);
		when(mutation.apply(any())).thenAnswer(invocation -> {
			KDLNode node = invocation.getArgument(0);
			return Optional.of(node.toBuilder().addArg(1).build());
		});

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node2 {node3; node4;}; node5 {node6 1;}"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(predicate1, times(1)).test(argThat(hasId("node5")));
		verify(predicate2, times(1)).test(argThat(hasId("node6")));
		verify(mutation, times(1)).apply(argThat(hasId("node6")));
		verifyNoMoreInteractions(predicate1, predicate2, mutation);
	}

	@Test
	public void testMutateNothingAtFirstLevelEverythingAtSecondLevelMatches() {
		var inputDoc = parser.parse("node1; node2 {node3; node4;}; node5 {node6;}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.build();
		when(predicate1.test(any())).thenReturn(false);

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node2 {node3; node4;}; node5 {node6;}"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(predicate1, times(1)).test(argThat(hasId("node5")));
		verifyNoMoreInteractions(predicate1, predicate2, mutation);
	}

	@Test
	public void testMutateEverythingAtFirstLevelNothingAtSecondLevelMatches() {
		var inputDoc = parser.parse("node1; node2 {node3; node4;}; node5 {node6;}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(false);

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node2 {node3; node4;}; node5 {node6;}"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(predicate1, times(1)).test(argThat(hasId("node5")));
		verify(predicate2, times(1)).test(argThat(hasId("node3")));
		verify(predicate2, times(1)).test(argThat(hasId("node4")));
		verify(predicate2, times(1)).test(argThat(hasId("node6")));
		verifyNoMoreInteractions(predicate1, predicate2, mutation);
	}

	@Test
	public void testListEmpty() {
		var inputDoc = KDLDocument.empty();
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.build();

		assertThat(search.list(inputDoc, false)).isEqualTo(KDLDocument.empty());
		verifyNoInteractions(predicate1);
	}

	@Test
	public void testListNothingMatches() {
		var inputDoc = parser.parse("node1; node2");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.build();
		when(predicate1.test(any())).thenReturn(false);

		assertThat(search.list(inputDoc, false)).isEqualTo(KDLDocument.empty());
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verifyNoMoreInteractions(predicate1);
	}

	@Test
	public void testListEverythingMatches() {
		var inputDoc = parser.parse("node1; node2 {node3;}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.build();
		when(predicate1.test(any())).thenReturn(true);

		assertThat(search.list(inputDoc, false)).isEqualTo(parser.parse("node1; node2 {node3;}"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verifyNoMoreInteractions(predicate1);
	}

	@Test
	public void testListEverythingMatchesTrim() {
		var inputDoc = parser.parse("node1; node2 {node3;}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.build();
		when(predicate1.test(any())).thenReturn(true);

		assertThat(search.list(inputDoc, true)).isEqualTo(parser.parse("node1; node2"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verifyNoMoreInteractions(predicate1);
	}

	@Test
	public void testListBranch() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(true);

		assertThat(search.list(inputDoc, false)).isEqualTo(parser.parse("node3 {node4;}"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(predicate2, times(1)).test(argThat(hasId("node3")));
		verifyNoMoreInteractions(predicate1, predicate2);
	}

	@Test
	public void testListBranchTrim() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(true);

		assertThat(search.list(inputDoc, true)).isEqualTo(parser.parse("node3"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(predicate2, times(1)).test(argThat(hasId("node3")));
		verifyNoMoreInteractions(predicate1, predicate2);
	}

	@Test
	public void testListLeaf() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.addLevel(predicate3)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(true);
		when(predicate3.test(any())).thenReturn(true);

		assertThat(search.list(inputDoc, false)).isEqualTo(parser.parse("node4"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(predicate2, times(1)).test(argThat(hasId("node3")));
		verify(predicate3, times(1)).test(argThat(hasId("node4")));
		verifyNoMoreInteractions(predicate1, predicate2, predicate3);
	}

	@Test
	public void testListLeafTrim() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.addLevel(predicate3)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(true);
		when(predicate3.test(any())).thenReturn(true);

		assertThat(search.list(inputDoc, true)).isEqualTo(parser.parse("node4"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(predicate2, times(1)).test(argThat(hasId("node3")));
		verify(predicate3, times(1)).test(argThat(hasId("node4")));
		verifyNoMoreInteractions(predicate1, predicate2, predicate3);
	}

	@Test
	public void testAnyMatchEmpty() {
		var inputDoc = KDLDocument.empty();
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.build();

		assertThat(search.anyMatch(inputDoc)).isFalse();
		verifyNoInteractions(predicate1);
	}

	@Test
	public void testAnyMatchNothingMatches() {
		var inputDoc = parser.parse("node1; node2 {node3;};");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.build();
		when(predicate1.test(any())).thenReturn(false);

		assertThat(search.anyMatch(inputDoc)).isFalse();
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verifyNoMoreInteractions(predicate1, predicate2);
	}

	@Test
	public void testAnyMatchNothingMatchesAtFirstLevel() {
		var inputDoc = parser.parse("node1; node2 {node3;};");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.build();
		when(predicate1.test(any())).thenReturn(false);

		assertThat(search.anyMatch(inputDoc)).isFalse();
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verifyNoMoreInteractions(predicate1, predicate2);
	}

	@Test
	public void testAnyMatchRoot() {
		var inputDoc = parser.parse("node1; node2 {node3;};");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.build();
		when(predicate1.test(any())).thenReturn(true);

		assertThat(search.anyMatch(inputDoc)).isTrue();
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verifyNoMoreInteractions(predicate1);
	}

	@Test
	public void testAnyMatchBranch() {
		var inputDoc = parser.parse("node1 {node2;}; node3 {node4 {node5;};};");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(false);
		when(predicate2.test(argThat(hasId("node4")))).thenReturn(true);

		assertThat(search.anyMatch(inputDoc)).isTrue();
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node3")));
		verify(predicate2, times(1)).test(argThat(hasId("node2")));
		verify(predicate2, times(1)).test(argThat(hasId("node4")));
		verifyNoMoreInteractions(predicate1, predicate2);
	}

	@Test
	public void testAnyMatchLeaf() {
		var inputDoc = parser.parse("node1 {node2;}; node3 {node4 {node5;};};");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.addLevel(predicate3)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(true);
		when(predicate3.test(any())).thenReturn(true);

		assertThat(search.anyMatch(inputDoc)).isTrue();
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node3")));
		verify(predicate2, times(1)).test(argThat(hasId("node2")));
		verify(predicate2, times(1)).test(argThat(hasId("node4")));
		verify(predicate3, times(1)).test(argThat(hasId("node5")));
		verifyNoMoreInteractions(predicate1, predicate2);
	}

	@Test
	public void testFilterEmpty() {
		var inputDoc = KDLDocument.empty();
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.build();

		assertThat(search.filter(inputDoc, true)).isEqualTo(KDLDocument.empty());
		verifyNoInteractions(predicate1);
	}

	@Test
	public void testFilterNothingMatches() {
		var inputDoc = parser.parse("node1; node2 {node3;}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(false);

		assertThat(search.filter(inputDoc, true)).isEqualTo(KDLDocument.empty());
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(predicate2, times(1)).test(argThat(hasId("node3")));
		verifyNoMoreInteractions(predicate1, predicate2);
	}

	@Test
	public void testFilterEverythingMatches() {
		var inputDoc = parser.parse("node1; node2 {node3;}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(true);

		assertThat(search.filter(inputDoc, true)).isEqualTo(parser.parse("node2 {node3;}"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(predicate2, times(1)).test(argThat(hasId("node3")));
		verifyNoMoreInteractions(predicate1, predicate2);
	}

	@Test
	public void testFilterRootMatches() {
		var inputDoc = parser.parse("node1; node2 {node3;}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.build();
		when(predicate1.test(any())).thenReturn(true);

		assertThat(search.filter(inputDoc, true)).isEqualTo(parser.parse("node1; node2"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verifyNoMoreInteractions(predicate1, predicate2);
	}

	@Test
	public void testFilterBranchMatches() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(true);

		assertThat(search.filter(inputDoc, true)).isEqualTo(parser.parse("node2 {node3;}"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(predicate2, times(1)).test(argThat(hasId("node3")));
		verifyNoMoreInteractions(predicate1, predicate2);
	}

	@Test
	public void testFilterLeafMatches() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.addLevel(predicate3)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(true);
		when(predicate3.test(any())).thenReturn(true);

		assertThat(search.filter(inputDoc, true)).isEqualTo(parser.parse("node2 {node3 {node4;};}"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(predicate2, times(1)).test(argThat(hasId("node3")));
		verify(predicate3, times(1)).test(argThat(hasId("node4")));
		verifyNoMoreInteractions(predicate1, predicate2, predicate3);
	}

	@Test
	public void testFilterMultipleLeavesMatch() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}; node5 {node6 {node7;};}");
		var search = PathedSearch.builder()
			.addLevel(predicate1)
			.addLevel(predicate2)
			.addLevel(predicate3)
			.build();
		when(predicate1.test(any())).thenReturn(true);
		when(predicate2.test(any())).thenReturn(true);
		when(predicate3.test(any())).thenReturn(true);

		assertThat(search.filter(inputDoc, true)).isEqualTo(parser.parse("node2 {node3 {node4;};}; node5 {node6 {node7;};}"));
		verify(predicate1, times(1)).test(argThat(hasId("node1")));
		verify(predicate1, times(1)).test(argThat(hasId("node2")));
		verify(predicate1, times(1)).test(argThat(hasId("node5")));
		verify(predicate2, times(1)).test(argThat(hasId("node3")));
		verify(predicate2, times(1)).test(argThat(hasId("node6")));
		verify(predicate3, times(1)).test(argThat(hasId("node4")));
		verify(predicate3, times(1)).test(argThat(hasId("node7")));
		verifyNoMoreInteractions(predicate1, predicate2, predicate3);
	}
}
