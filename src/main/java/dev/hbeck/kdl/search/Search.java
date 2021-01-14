package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface Search {
    KDLDocument filter();

    List<KDLNode> findAll();

    KDLDocument mutate(Function<KDLNode, Optional<KDLNode>>);
}
