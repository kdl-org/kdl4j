package dev.hbeck.kdl.search;

import dev.hbeck.kdl.search.mutation.Mutation;
import dev.hbeck.kdl.search.predicates.NodePredicate;

import java.util.Map;
import java.util.Optional;

public class Operation {
    private final Map<Integer, NodePredicate> path;
    private final Optional<Mutation> mutation;

    public Operation(Map<Integer, NodePredicate> path, Optional<Mutation> mutation) {
        this.path = path;
        this.mutation = mutation;
    }

    public Map<Integer, NodePredicate> getPath() {
        return path;
    }

    public Optional<Mutation> getMutation() {
        return mutation;
    }

    public int getDepth() {
        return path.size();
    }
}
