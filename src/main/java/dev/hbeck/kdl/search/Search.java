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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
                if (nodeMatches(node)) {
                    nodes.add(node);
                }
            }
        }

        if (depth <= maxDepth) {
            for (KDLNode node : doc.getNodes()) {
                search(doc, depth + 1, nodes);
            }
        }

        return nodes;
    }

    public KDLDocument filter() {
        return filter(document, 0).orElse(KDLDocument.empty());
    }

    private Optional<KDLDocument> filter(KDLDocument doc, int depth) {
        if (depth > maxDepth) {
            return Optional.empty();
        }

        final KDLDocument.Builder builder = KDLDocument.builder();
        for (KDLNode node : doc.getNodes()) {
            if (nodeMatches(node)) {
                if (node.getChild().isPresent()) {
                    final Optional<KDLDocument> newChild = node.getChild().flatMap(ch -> filter(ch, depth + 1));
                    builder.addNode(node.toBuilder().setChild(newChild).build());
                } else {
                    builder.addNode(node);
                }
            }
        }

        final KDLDocument returnDoc = builder.build();
        if (returnDoc.getNodes().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(returnDoc);
        }
    }

    public KDLDocument mutate(Function<KDLNode, Optional<KDLNode>> fun) {
        return mutate(fun, document, 0).orElse(KDLDocument.empty());
    }

    private Optional<KDLDocument> mutate(Function<KDLNode, Optional<KDLNode>> fun, KDLDocument doc, int depth) {
        if (depth > maxDepth) {
            return Optional.of(doc);
        }

        final KDLDocument.Builder docBuilder = KDLDocument.builder();
        if (depth < minDepth || maxDepth < depth) {
            for (KDLNode node : doc.getNodes()) {
                if (nodeMatches(node)) {
                    if (node.getChild().isPresent()) {
                        final Optional<KDLDocument> newChild = node.getChild().flatMap(ch -> mutate(fun, ch, depth + 1));
                        final KDLNode newNode = node.toBuilder().setChild(newChild).build();
                        fun.apply(newNode).ifPresent(docBuilder::addNode);
                    } else {
                        fun.apply(node).ifPresent(docBuilder::addNode);
                    }
                }
            }
        }

        final KDLDocument newDoc = docBuilder.build();
        if (newDoc.getNodes().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(newDoc);
        }
    }

    private boolean nodeMatches(KDLNode node) {
        boolean matchedIdentifier = nodeIdentifiers.isEmpty();
        for (Predicate<String> pred : nodeIdentifiers) {
            matchedIdentifier |= pred.test(node.getIdentifier());
        }

        if (!matchedIdentifier) {
            return false;
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
            return false;
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

        return (matchAllProps || allPropsHereMatch) && anyPropsHereMatch;
    }

    public static Search of(KDLDocument document) {
        return new Search(document);
    }
}
