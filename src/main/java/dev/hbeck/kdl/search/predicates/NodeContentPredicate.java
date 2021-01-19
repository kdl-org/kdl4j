package dev.hbeck.kdl.search.predicates;

import dev.hbeck.kdl.objects.KDLNode;

import java.util.function.Predicate;

/**
 * Interface implemented by all predicates that operate on the contents of a node, where contents are arguments,
 * properties, and child
 */
public interface NodeContentPredicate extends Predicate<KDLNode> {

    static NodeContentPredicate any() {
        return node -> true;
    }
}
