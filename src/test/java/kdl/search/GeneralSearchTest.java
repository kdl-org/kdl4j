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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GeneralSearchTest {
	private static final KDLParser parser = new KDLParser();

	@Mock
	public Mutation mutation;

	@Mock
	public NodePredicate predicate;

	@Test
	public void testMutateEmpty() {
		var inputDoc = KDLDocument.empty();
		var search = GeneralSearch.builder().setPredicate(predicate).build();

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(inputDoc);
		verifyNoInteractions(mutation);
	}

	@Test
	public void testMutateEmptyWithPredicate() {
		var inputDoc = KDLDocument.empty();
		var search = GeneralSearch.builder().setPredicate(predicate).build();

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(inputDoc);
		verifyNoInteractions(mutation);
	}

	@Test
	public void testMutateNothingMatchesPredicate() {
		var inputDoc = parser.parse("node1; node2 {node4;}; node3");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(inputDoc);
		verifyNoInteractions(mutation);
	}

	@Test
	public void testMutateRootNodeMatchesPredicateDelete() {
		var inputDoc = parser.parse("node1; node2; node3");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node2")))).thenReturn(true);
		when(mutation.apply(any())).thenReturn(Optional.empty());

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node3"));
		verify(mutation, times(1)).apply(argThat(hasId("node2")));
	}

	@Test
	public void testMutateBranchNodeMatchesPredicateDelete() {
		var inputDoc = parser.parse("node1; node2 {node4 {node5;};}; node3");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
		when(mutation.apply(any())).thenReturn(Optional.empty());

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node2; node3"));
		verify(mutation, times(1)).apply(argThat(hasId("node4")));
	}

	@Test
	public void testMutateLeafNodeMatchesPredicateDelete() {
		var inputDoc = parser.parse("node1; node2 {node4;}; node3");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
		when(mutation.apply(any())).thenReturn(Optional.empty());

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node2; node3"));
		verify(mutation, times(1)).apply(argThat(hasId("node4")));
	}

	@Test
	public void testMutateEverythingMatchesPredicate() {
		var inputDoc = parser.parse("node1; node2 {node4 {node5;};}; node3");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(true);
		when(mutation.apply(any())).thenAnswer(invocation -> {
			KDLNode node = invocation.getArgument(0);
			return Optional.of(node.toBuilder().addArg(1).build());
		});

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1 1; node2 1 {node4 1 {node5 1;};}; node3 1"));
		verify(mutation, times(5)).apply(any());
		verifyNoMoreInteractions(mutation);
	}

	@Test
	public void testMutateRootNodeMatchesPredicate() {
		var inputDoc = parser.parse("node1; node2; node3");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node2")))).thenReturn(true);
		when(mutation.apply(any())).thenAnswer(invocation -> {
			KDLNode node = invocation.getArgument(0);
			return Optional.of(node.toBuilder().addArg(1).build());
		});

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node2 1; node3"));
		verify(mutation, times(1)).apply(argThat(hasId("node2")));
	}

	@Test
	public void testMutateBranchNodeMatchesPredicate() {
		var inputDoc = parser.parse("node1; node2 {node4 {node5;};}; node3");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
		when(mutation.apply(any())).thenAnswer(invocation -> {
			KDLNode node = invocation.getArgument(0);
			return Optional.of(node.toBuilder().addArg(1).build());
		});

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node2 {node4 1 {node5;};}; node3"));
		verify(mutation, times(1)).apply(argThat(hasId("node4")));
	}

	@Test
	public void testMutateLeafNodeMatchesPredicate() {
		var inputDoc = parser.parse("node1; node2 {node4;}; node3");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
		when(mutation.apply(any())).thenAnswer(invocation -> {
			KDLNode node = invocation.getArgument(0);
			return Optional.of(node.toBuilder().addArg(1).build());
		});

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node2 {node4 1;}; node3"));
		verify(mutation, times(1)).apply(argThat(hasId("node4")));
	}

	@Test
	public void testMutateEverythingMatchesPredicateAddChild() {
		var inputDoc = parser.parse("node1; node2 {node4 {node5;};}; node3");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(true);
		when(mutation.apply(any())).thenAnswer(invocation -> {
			KDLNode node = invocation.getArgument(0);
			var docBuilder = KDLDocument.builder();
			node.getChild().ifPresent(ch -> docBuilder.addNodes(ch.getNodes()));
			docBuilder.addNode(KDLNode.builder().setIdentifier("added").build());

			return Optional.of(node.toBuilder().setChild(docBuilder.build()).build());
		});

		assertThat(search.mutate(inputDoc, mutation))
			.isEqualTo(parser.parse("node1 {added;}; node2 {node4 {node5 {added;}; added;}; added;}; node3 {added;}"));
		verify(mutation, times(5)).apply(any());
		verifyNoMoreInteractions(mutation);
	}

	@Test
	public void testMutateRootNodeMatchesPredicateAddChild() {
		var inputDoc = parser.parse("node1; node2; node3");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node2")))).thenReturn(true);
		when(mutation.apply(any())).thenAnswer(invocation -> {
			KDLNode node = invocation.getArgument(0);
			var docBuilder = KDLDocument.builder();
			node.getChild().ifPresent(ch -> docBuilder.addNodes(ch.getNodes()));
			docBuilder.addNode(KDLNode.builder().setIdentifier("added").build());

			return Optional.of(node.toBuilder().setChild(docBuilder.build()).build());
		});

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node2 {added;}; node3"));
		verify(mutation, times(1)).apply(argThat(hasId("node2")));
	}

	@Test
	public void testMutateBranchNodeMatchesPredicateAddChild() {
		var inputDoc = parser.parse("node1; node2 {node4 {node5;};}; node3");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
		when(mutation.apply(any())).thenAnswer(invocation -> {
			KDLNode node = invocation.getArgument(0);
			var docBuilder = KDLDocument.builder();
			node.getChild().ifPresent(ch -> docBuilder.addNodes(ch.getNodes()));
			docBuilder.addNode(KDLNode.builder().setIdentifier("added").build());

			return Optional.of(node.toBuilder().setChild(docBuilder.build()).build());
		});

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node2 {node4 {node5; added;};}; node3"));
		verify(mutation, times(1)).apply(argThat(hasId("node4")));
	}

	@Test
	public void testMutateLeafNodeMatchesPredicateAddChild() {
		var inputDoc = parser.parse("node1; node2 {node4;}; node3");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
		when(mutation.apply(any())).thenAnswer(invocation -> {
			KDLNode node = invocation.getArgument(0);
			var docBuilder = KDLDocument.builder();
			node.getChild().ifPresent(ch -> docBuilder.addNodes(ch.getNodes()));
			docBuilder.addNode(KDLNode.builder().setIdentifier("added").build());

			return Optional.of(node.toBuilder().setChild(docBuilder.build()).build());
		});

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node2 {node4 {added;};}; node3"));
		verify(mutation, times(1)).apply(argThat(hasId("node4")));
	}

	@Test
	public void testMutateMinDepthAddChild() {
		var inputDoc = parser.parse("node1; node2 {node4;}; node3");
		var search = GeneralSearch.builder()
			.setPredicate(predicate)
			.setMaxDepth(0)
			.build();
		when(predicate.test(any())).thenReturn(false);

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(inputDoc);
		verify(mutation, never()).apply(any());
	}

	@Test
	public void testMutateEverythingMatchesButDepth() {
		var inputDoc = parser.parse("node1; node2 {node4 {node5;};}; node3");
		var search = GeneralSearch.builder()
			.setPredicate(predicate)
			.setMinDepth(1)
			.setMaxDepth(1)
			.build();
		when(predicate.test(any())).thenReturn(true);
		when(mutation.apply(any())).thenAnswer(invocation -> {
			KDLNode node = invocation.getArgument(0);
			return Optional.of(node.toBuilder().addArg(1).build());
		});

		assertThat(search.mutate(inputDoc, mutation)).isEqualTo(parser.parse("node1; node2 {node4 1 {node5;};}; node3"));
		verify(mutation, times(1)).apply(argThat(hasId("node4")));
		verifyNoMoreInteractions(mutation);
	}

	@Test
	public void testFilterEmpty() {
		var inputDoc = KDLDocument.empty();
		var search = GeneralSearch.builder().setPredicate(predicate).build();

		assertThat(search.filter(inputDoc, true)).isEqualTo(KDLDocument.empty());
	}

	@Test
	public void testFilterNothingMatches() {
		var inputDoc = parser.parse("node1; node2 {node3;}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();

		assertThat(search.filter(inputDoc, true)).isEqualTo(KDLDocument.empty());
	}

	@Test
	public void testFilterRootNodeMatches() {
		var inputDoc = parser.parse("node1; node2 {node3;}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node2")))).thenReturn(true);

		assertThat(search.filter(inputDoc, true)).isEqualTo(parser.parse("node2"));
	}

	@Test
	public void testFilterBranchNodeMatches() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node3")))).thenReturn(true);

		assertThat(search.filter(inputDoc, true)).isEqualTo(parser.parse("node2 {node3;}"));
	}

	@Test
	public void testFilterLeafNodeMatches() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node4")))).thenReturn(true);

		assertThat(search.filter(inputDoc, true)).isEqualTo(parser.parse("node2 {node3 {node4;};}"));
	}

	@Test
	public void testFilterMultipleLeavesMatch() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4; node5;};}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node4")))).thenReturn(true);
		when(predicate.test(argThat(hasId("node5")))).thenReturn(true);

		assertThat(search.filter(inputDoc, true)).isEqualTo(parser.parse("node2 {node3 {node4; node5;};}"));
	}

	@Test
	public void testFilterEverythingMatches() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(true);

		assertThat(search.filter(inputDoc, true)).isEqualTo(parser.parse("node1; node2 {node3 {node4;};}"));
	}

	@Test
	public void testListEmpty() {
		var inputDoc = KDLDocument.empty();
		var search = GeneralSearch.builder().setPredicate(predicate).build();

		assertThat(search.list(inputDoc, false)).isEqualTo(KDLDocument.empty());
	}

	@Test
	public void testListNothingMatches() {
		var inputDoc = parser.parse("node1; node2");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);

		assertThat(search.list(inputDoc, false)).isEqualTo(KDLDocument.empty());
	}

	@Test
	public void testListNothingMatchesTrim() {
		var inputDoc = parser.parse("node1; node2");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);

		assertThat(search.list(inputDoc, true)).isEqualTo(KDLDocument.empty());
	}

	@Test
	public void testListEverythingMatches() {
		var inputDoc = parser.parse("node1; node2 {node3;}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(true);

		assertThat(search.list(inputDoc, false)).isEqualTo(parser.parse("node1; node2 {node3;}; node3"));
	}

	@Test
	public void testListEverythingMatchesTrim() {
		var inputDoc = parser.parse("node1; node2 {node3;}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(true);

		assertThat(search.list(inputDoc, true)).isEqualTo(parser.parse("node1; node2; node3"));
	}

	@Test
	public void testListRootNodeMatches() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node2")))).thenReturn(true);

		assertThat(search.list(inputDoc, false)).isEqualTo(parser.parse("node2 {node3 {node4;};}"));
	}

	@Test
	public void testListRootNodeMatchesTrim() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node2")))).thenReturn(true);

		assertThat(search.list(inputDoc, true)).isEqualTo(parser.parse("node2"));
	}

	@Test
	public void testListBranchNodeMatches() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node3")))).thenReturn(true);

		assertThat(search.list(inputDoc, false)).isEqualTo(parser.parse("node3 {node4;}"));
	}

	@Test
	public void testListBranchNodeMatchesTrim() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node3")))).thenReturn(true);

		assertThat(search.list(inputDoc, true)).isEqualTo(parser.parse("node3"));
	}

	@Test
	public void testListLeafNodeMatches() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node4")))).thenReturn(true);

		assertThat(search.list(inputDoc, false)).isEqualTo(parser.parse("node4"));
	}

	@Test
	public void testListLeafNodeMatchesTrim() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node4")))).thenReturn(true);

		assertThat(search.list(inputDoc, true)).isEqualTo(parser.parse("node4"));
	}

	@Test
	public void testAnyMatchEmpty() {
		var inputDoc = KDLDocument.empty();
		var search = GeneralSearch.builder().setPredicate(predicate).build();

		assertThat(search.anyMatch(inputDoc)).isFalse();
		verifyNoInteractions(predicate);
	}

	@Test
	public void testAnyMatchNothingMatches() {
		var inputDoc = parser.parse("node1; node2");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);

		assertThat(search.anyMatch(inputDoc)).isFalse();
		verify(predicate, times(1)).test(argThat(hasId("node1")));
		verify(predicate, times(1)).test(argThat(hasId("node2")));
	}

	@Test
	public void testAnyMatchEverythingMatches() {
		var inputDoc = parser.parse("node1; node2");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(true);

		assertThat(search.anyMatch(inputDoc)).isTrue();
		verify(predicate, never()).test(argThat(hasId("node2")));
	}

	@Test
	public void testAnyMatchRootNodeMatches() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node2")))).thenReturn(true);

		assertThat(search.anyMatch(inputDoc)).isTrue();
		verify(predicate, times(1)).test(argThat(hasId("node1")));
		verify(predicate, times(1)).test(argThat(hasId("node2")));
		verify(predicate, never()).test(argThat(hasId("node3")));
		verify(predicate, never()).test(argThat(hasId("node4")));
	}

	@Test
	public void testAnyMatchBranchNodeMatches() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node3")))).thenReturn(true);

		assertThat(search.anyMatch(inputDoc)).isTrue();
		verify(predicate, times(1)).test(argThat(hasId("node1")));
		verify(predicate, times(1)).test(argThat(hasId("node2")));
		verify(predicate, times(1)).test(argThat(hasId("node3")));
		verify(predicate, never()).test(argThat(hasId("node4")));
	}

	@Test
	public void testAnyMatchLeafNodeMatches() {
		var inputDoc = parser.parse("node1; node2 {node3 {node4;};}");
		var search = GeneralSearch.builder().setPredicate(predicate).build();
		when(predicate.test(any())).thenReturn(false);
		when(predicate.test(argThat(hasId("node4")))).thenReturn(true);

		assertThat(search.anyMatch(inputDoc)).isTrue();
		verify(predicate, times(1)).test(argThat(hasId("node1")));
		verify(predicate, times(1)).test(argThat(hasId("node2")));
		verify(predicate, times(1)).test(argThat(hasId("node3")));
		verify(predicate, times(1)).test(argThat(hasId("node4")));
	}
}
