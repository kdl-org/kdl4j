package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.search.mutation.Mutation;

import java.util.List;

public class PathedSearch {
    public PathedSearch(KDLDocument document) {
        this.document = document;
    }

    private final KDLDocument document;

    public List<KDLNode> findAll() {

    }

    public KDLDocument applyMutation(Mutation mutation) {

    }
}
