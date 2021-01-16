package dev.hbeck.kdl.search.predicates;

import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.search.Search;

import java.util.List;
import java.util.Optional;

/**
 * We need to represent 2 types of child searches:
 * - Searches for nodes with missing or empty children
 * - Searches for nodes with any children matching a Search
 */
public class ChildPredicate implements NodeContentPredicate {
    private final Optional<Search> search;

    public ChildPredicate(Optional<Search> search) {
        this.search = search;
    }

    @Override
    public boolean test(KDLNode node) {
        if (!search.isPresent()) {
            return !node.getChild().isPresent() || node.getChild().get().getNodes().isEmpty();
        }

        if (!node.getChild().isPresent() || node.getChild().get().getNodes().isEmpty()) {
            return false;
        } else {
            final List<KDLNode> found = search.get().listAll(node.getChild().get(), true);
            return !found.isEmpty();
        }
    }

    public static ChildPredicate empty() {
        return new ChildPredicate(Optional.empty());
    }
}
