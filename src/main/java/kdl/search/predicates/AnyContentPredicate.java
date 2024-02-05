package kdl.search.predicates;

import kdl.objects.KDLNode;

/**
 * Returns true if a node has any contents, false otherwise
 */
public class AnyContentPredicate implements NodeContentPredicate {
    @Override
    public boolean test(KDLNode node) {
        return !node.getArgs().isEmpty()
                || !node.getProps().isEmpty()
                || (node.getChild().isPresent() && !node.getChild().get().getNodes().isEmpty());
    }
}
