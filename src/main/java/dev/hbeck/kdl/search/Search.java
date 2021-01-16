package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.search.mutation.Mutation;

import java.util.List;

public interface Search {
    KDLDocument filter(KDLDocument document);

    List<KDLNode> listAll(KDLDocument document, boolean trim);

    KDLDocument mutate(KDLDocument document, Mutation mutation);
}
