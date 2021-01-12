package dev.hbeck.kdl.search.mutation;

import dev.hbeck.kdl.objects.KDLNode;

import java.util.Optional;

public interface Mutation {
    Optional<KDLNode> apply(KDLNode node);
}
