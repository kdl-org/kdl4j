package dev.hbeck.kdl.search.mutation;

import dev.hbeck.kdl.objects.KDLNode;

import java.util.Optional;
import java.util.function.Function;

public interface Mutation extends Function<KDLNode, Optional<KDLNode>> {
}
