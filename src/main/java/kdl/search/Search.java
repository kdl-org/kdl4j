package kdl.search;

import kdl.objects.KDLDocument;
import kdl.search.mutation.Mutation;

/**
 * Interface implemented by all search types
 */
public interface Search {

    /**
     * Filter branches in the document to only those containing nodes matching the search.
     *
     * @param document the document to search
     * @param trim if true, trim the resulting tree such that all leaves match the search
     * @return a new document filtered to branches with matching nodes
     */
    KDLDocument filter(KDLDocument document, boolean trim);

    /**
     * Creates a new document with all nodes matching the search promoted to the root. If trim is not set a given node
     * may appear multiple times in the resulting document if multiple nodes on a branch match the search.
     *
     * @param document the document to search
     * @param trim if true, trim the children off of all resulting nodes
     * @return a new document with all matching nodes at the root
     */
    KDLDocument list(KDLDocument document, boolean trim);

    /**
     * Applies a mutation to all nodes in the tree matching the search in a depth-first fashion, returning the full resulting tree
     *
     * @param document the document to mutate
     * @param mutation the mutation to apply to all matching nodes
     * @return a new document with the mutation applied
     */
    KDLDocument mutate(KDLDocument document, Mutation mutation);

    /**
     * Searches for any matching nodes in the tree
     *
     * @param document the document to search
     * @return true if a matching node is found, or false otherwise
     */
    boolean anyMatch(KDLDocument document);
}
