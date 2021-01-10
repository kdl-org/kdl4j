package dev.hbeck.kdl.search;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class Search {
    private final Set<Predicate<String>> nodeIdentifiers = new HashSet<>();
    private final Set<Predicate<KDLValue>> args = new HashSet<>();
    private final Map<Predicate<String>, Predicate<KDLValue>> properties = new HashMap<>();

    private boolean matchAllArgs = false;
    private boolean matchAllProps = false;
    private int minDepth = 0;
    private int maxDepth = Integer.MAX_VALUE;

    private final KDLDocument document;

    public Search(KDLDocument document) {
        this.document = Objects.requireNonNull(document);
    }

    public Search forNodeId(String identifier) {
        nodeIdentifiers.add(Predicate.isEqual(identifier));
        return this;
    }

    public Search forNodeId(Predicate<String> identifier) {
        nodeIdentifiers.add(Predicate.isEqual(identifier));
        return this;
    }

    public Search matchAllArgs() {
        matchAllArgs = true;
        return this;
    }

    public Search matchAllProps() {
        matchAllProps = true;
        return this;
    }

    public Search forProperty(String property, KDLValue value) {
        properties.put(Predicate.isEqual(property), Predicate.isEqual(value));
        return this;
    }

    public Search forProperty(Predicate<String> property, KDLValue value) {
        properties.put(property, Predicate.isEqual(value));
        return this;
    }

    public Search forProperty(String property, Predicate<KDLValue> value) {
        properties.put(Predicate.isEqual(property), value);
        return this;
    }

    public Search forProperty(Predicate<String> property, Predicate<KDLValue> value) {
        properties.put(property, value);
        return this;
    }

    public Search forArg(KDLValue arg) {
        args.add(Predicate.isEqual(arg));
        return this;
    }

    public Search forArg(Predicate<KDLValue> arg) {
        args.add(arg);
        return this;
    }

    public Search setMinDepth(int minDepth) {
        this.minDepth = minDepth;
        return this;
    }

    public Search setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public List<KDLNode> search() {
        return Collections.unmodifiableList(search(document, 0, new ArrayList<>()));
    }

    private List<KDLNode> search(KDLDocument doc, int depth, List<KDLNode> nodes) {
        if (depth < minDepth || maxDepth < depth) {
            for (KDLNode node : doc.getNodes()) {
                boolean matchedIdentifier = nodeIdentifiers.isEmpty();
                for (Predicate<String> pred : nodeIdentifiers) {
                    matchedIdentifier |= pred.test(node.getIdentifier());
                }

                if (!matchedIdentifier) {
                    continue;
                }

                boolean anyArgsHereMatch = false;
                boolean allArgsHereMatch = true;
                for (KDLValue arg : node.getArgs()) {
                    for (Predicate<KDLValue> pred : args) {
                        boolean matches = pred.test(arg);
                        anyArgsHereMatch |= matches;
                        allArgsHereMatch &= matches;
                    }
                }

                if ((matchAllArgs && !allArgsHereMatch) || !anyArgsHereMatch) {
                    continue;
                }

                boolean anyPropsHereMatch = false;
                boolean allPropsHereMatch = true;
                for (Map.Entry<String, KDLValue> prop : node.getProps().entrySet()) {
                    for (Map.Entry<Predicate<String>, Predicate<KDLValue>> propPred : properties.entrySet()) {
                        boolean matches = propPred.getKey().test(prop.getKey()) && propPred.getValue().test(prop.getValue());
                        anyPropsHereMatch |= matches;
                        allPropsHereMatch &= matches;
                    }
                }

                if ((!matchAllProps && !allPropsHereMatch) || !anyPropsHereMatch) {
                    continue;
                }

                nodes.add(node);
            }
        }

        if (depth <= maxDepth) {
            for (KDLNode node : doc.getNodes()) {
                search(doc, depth + 1, nodes);
            }
        }

        return nodes;
    }

    public static Search of(KDLDocument document) {
        return new Search(document);
    }
}
