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
        nodeIdentifiers.add(identifier);
        return this;
    }

    public Search matchAllArgPredicates() {
        matchAllArgs = true;
        return this;
    }

    public Search matchAllPropPredicates() {
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
        if (depth <= maxDepth) {
            for (KDLNode node : doc.getNodes()) {
                if (minDepth <= depth && nodeMatches(node)) {
                    nodes.add(node);
                }

                node.getChild().ifPresent(ch -> search(ch, depth + 1, nodes));
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
        for (KDLNode node : doc.getNodes()) {
            if (depth >= minDepth && nodeMatches(node)) {
                if (node.getChild().isPresent()) {
                    final Optional<KDLDocument> newChild = node.getChild().flatMap(ch -> mutate(fun, ch, depth + 1));
                    final KDLNode newNode = node.toBuilder().setChild(newChild).build();
                    fun.apply(newNode).ifPresent(docBuilder::addNode);
                } else {
                    fun.apply(node).ifPresent(docBuilder::addNode);
                }
            } else {
                final Optional<KDLDocument> newChild = node.getChild().flatMap(ch -> mutate(fun, ch, depth + 1));
                docBuilder.addNode(node.toBuilder().setChild(newChild).build());
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

        if (matchAllArgs && !args.isEmpty() && node.getArgs().isEmpty()) {
            return false;
        }

        boolean anyArgsHereMatch = args.isEmpty();
        boolean allArgsHereMatch = true;
        for (Predicate<KDLValue> pred : args) {
            boolean someArgMatched = false;
            for (KDLValue arg : node.getArgs()) {
                someArgMatched |= pred.test(arg);
            }
            anyArgsHereMatch |= someArgMatched;
            allArgsHereMatch &= someArgMatched;
        }

        if ((matchAllArgs && !allArgsHereMatch) || !anyArgsHereMatch) {
            return false;
        }

        if (matchAllProps && !properties.isEmpty() && node.getProps().isEmpty()) {
            return false;
        }

        boolean anyPropsHereMatch = properties.isEmpty();
        boolean allPropsHereMatch = true;
        for (Map.Entry<Predicate<String>, Predicate<KDLValue>> propPred : properties.entrySet()) {
            boolean somePropMatched = false;
            for (Map.Entry<String, KDLValue> prop : node.getProps().entrySet()) {
                somePropMatched |= propPred.getKey().test(prop.getKey()) && propPred.getValue().test(prop.getValue());
            }
            anyPropsHereMatch |= somePropMatched;
            allPropsHereMatch &= somePropMatched;
        }

        if (matchAllProps) {
            return allPropsHereMatch;
        } else {
            return anyPropsHereMatch;
        }
    }

    public static <T> Predicate<T> any() {
        return v -> true;
    }

    public static Search of(KDLDocument document) {
        return new Search(document);
    }
}
