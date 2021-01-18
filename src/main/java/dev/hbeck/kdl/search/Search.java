package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.search.mutation.Mutation;

public interface Search {
    KDLDocument filter(KDLDocument document);

    KDLDocument list(KDLDocument document, boolean trim);

    KDLDocument mutate(KDLDocument document, Mutation mutation);

    boolean anyMatch(KDLDocument document);
}
