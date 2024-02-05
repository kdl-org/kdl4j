package kdl.search.mutation;

import kdl.objects.KDLNode;

import java.util.Optional;
import java.util.function.Function;

/**
 * Interface implemented by all classes or lambdas that make changes to a document tree based on a Search. If an
 * implementation returns Optional.empty() the node will be deleted.
 */
public interface Mutation extends Function<KDLNode, Optional<KDLNode>> {
}
