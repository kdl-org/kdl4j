package dev.hbeck.kdl.search.predicates;

import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.search.GeneralSearch;

import java.util.function.Predicate;

public class NodePredicate implements Predicate<KDLNode> {
    private final Predicate<String> identifierPredicate;
    private final Predicate<Boolean> childPredicate;
    private final Predicate<KDLNode> nodeContentPredicate;

    public NodePredicate(Predicate<String> identifierPredicate, Predicate<Boolean> childPredicate, Predicate<KDLNode> nodeContentPredicate) {
        this.identifierPredicate = identifierPredicate;
        this.childPredicate = childPredicate;
        this.nodeContentPredicate = nodeContentPredicate;
    }

    @Override
    public boolean test(KDLNode node) {
        if (!identifierPredicate.test(node.getIdentifier())) {
            return false;
        } else if (!nodeContentPredicate.test(node)) {
            return false;
        } else {
            return childPredicate.test(node.getChild().isPresent());
        }
    }

    public static NodePredicate any() {
        return new NodePredicate(GeneralSearch.any(), GeneralSearch.any(), GeneralSearch.any());
    }
}
