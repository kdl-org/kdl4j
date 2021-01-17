package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;

public interface Search {
    KDLDocument filter(KDLDocument document);

    KDLDocument list(KDLDocument document, boolean trim);
}
