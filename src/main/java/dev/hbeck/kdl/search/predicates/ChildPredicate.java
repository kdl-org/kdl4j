package dev.hbeck.kdl.search.predicates;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.search.Search;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * We need to represent 3 types of child searches:
 * - Searches for nodes with missing or empty children
 * - Searches for nodes with any children matching a predicate
 * - Searches
 */
public class ChildPredicate implements NodeContentPredicate {
    private final Optional<Function<KDLDocument, Search>> subPathedSearch;
    private final Optional<Function<KDLDocument, Search>> subGeneralSearch;

    private ChildPredicate(Optional<Function<KDLDocument, Search>> subPathedSearch, Optional<Function<KDLDocument, Search>> subGeneralSearch) {
        this.subPathedSearch = subPathedSearch;
        this.subGeneralSearch = subGeneralSearch;
    }

    @Override
    public boolean test(KDLNode node) {
        final Function<KDLDocument, Search> search;
        if (!subGeneralSearch.isPresent() && !subPathedSearch.isPresent()) {
            return !node.getChild().isPresent() || node.getChild().get().getNodes().isEmpty();
        } else if (subPathedSearch.isPresent()) {
            search = subPathedSearch.get();
        } else if (subGeneralSearch.isPresent()) {
            search = subGeneralSearch.get();
        } else {
            throw new RuntimeException("Both pathed and general search were set, how???");
        }

        if (!node.getChild().isPresent() || node.getChild().get().getNodes().isEmpty()) {
            return false;
        } else {
            final List<KDLNode> found = search.apply(node.getChild().get()).listAll();
            return !found.isEmpty();
        }
    }

    public static ChildPredicate empty() {
        return new ChildPredicate(Optional.empty(), Optional.empty());
    }

    public static ChildPredicate pathedSearch(Function<KDLDocument, Search> searchFun) {
        return new ChildPredicate(Optional.of(searchFun), Optional.empty());
    }

    public static ChildPredicate generalSearch(Function<KDLDocument, Search> searchFun) {
        return new ChildPredicate(Optional.empty(), Optional.of(searchFun));
    }
}
