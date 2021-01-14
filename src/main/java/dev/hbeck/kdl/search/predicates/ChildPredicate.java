package dev.hbeck.kdl.search.predicates;

import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.search.PathedSearch;
import dev.hbeck.kdl.search.Search;

import java.util.Optional;

/**
 * We need to represent 3 types of child searches:
 *  - Searches for nodes with missing or empty children
 *  - Searches for nodes with any children matching a predicate
 *  - Searches
 */
public class ChildPredicate implements NodeContentPredicate {
    private final Optional<Boolean> isEmpty;
    private final Optional<PathedSearch> subPathedSearch;
    private final Optional<Search> subGeneralSearch;


    @Override
    public boolean test(KDLNode node) {



        return false;
    }
}
