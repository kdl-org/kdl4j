package kdl.search.predicates;

import kdl.objects.KDLNode;

/**
 * Matches any node with no content, ie only a name.
 */
public class EmptyContentPredicate implements NodeContentPredicate {
    @Override
    public boolean test(KDLNode node) {
        return node.getArgs().isEmpty() && node.getProps().isEmpty() && !node.getChild().isPresent();
    }
}
