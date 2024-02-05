package kdl.search.predicates;

import kdl.objects.KDLNode;
import kdl.search.Search;

import java.util.Optional;

/**
 * Matches nodes based on the contents, or absence, of a child
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

        return node.getChild().map(ch -> search.get().anyMatch(ch)).orElse(false);
    }

    public static ChildPredicate empty() {
        return new ChildPredicate(Optional.empty());
    }
}
