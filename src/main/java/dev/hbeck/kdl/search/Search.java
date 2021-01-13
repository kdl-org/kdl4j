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

/**
 * Represents searches of a document, as well as modifications to the document based on the search
 */
public class Search {
    private final Set<Predicate<String>> nodeIdentifiers = new HashSet<>();
    private final Set<Predicate<KDLValue>> args = new HashSet<>();
    private final Map<Predicate<String>, Predicate<KDLValue>> properties = new HashMap<>();

    private boolean matchAllArgs = false;
    private boolean matchAllProps = false;
    private int minDepth = 0;
    private int maxDepth = Integer.MAX_VALUE;

    private final KDLDocument document;

    private Search(KDLDocument document) {
        this.document = Objects.requireNonNull(document);
    }

    /**
     * Convenience method to create a predicate that matches all inputs
     *
     * @param <T> the type of object being matched, ignored
     * @return a predicate that always returns true
     */
    public static <T> Predicate<T> any() {
        return v -> true;
    }

    public static Search of(KDLDocument document) {
        return new Search(document);
    }

    /**
     * Search for a node with the literal identifier specified
     *
     * @param identifier the identifier to search for
     * @return the search for build chaining
     */
    public Search forNodeId(String identifier) {
        nodeIdentifiers.add(Predicate.isEqual(identifier));
        return this;
    }

    /**
     * Search for a node whose identifier matches the provided predicate
     *
     * @param identifier the identifier predicate
     * @return the search for build chaining
     */
    public Search forNodeId(Predicate<String> identifier) {
        nodeIdentifiers.add(identifier);
        return this;
    }

    /**
     * By default, nodes will match if *any* argument predicate matches, calling this will cause them to only match
     * if all argument predicates return true.
     *
     * @return the search for build chaining
     */
    public Search matchAllArgPredicates() {
        matchAllArgs = true;
        return this;
    }

    /**
     * By default, nodes will match if *any* property predicate matches, calling this will cause them to only match
     * if all property predicates return true.
     *
     * @return the search for build chaining
     */
    public Search matchAllPropPredicates() {
        matchAllProps = true;
        return this;
    }

    /**
     * Adds a property predicate
     *
     * @param property the property name
     * @param value the property value
     *
     * @return the search for build chaining
     */
    public Search forProperty(String property, KDLValue value) {
        properties.put(Predicate.isEqual(property), Predicate.isEqual(value));
        return this;
    }

    /**
     * Adds a property predicate
     *
     * @param property the property name
     * @param value the property value
     *
     * @return the search for build chaining
     */
    public Search forProperty(Predicate<String> property, KDLValue value) {
        properties.put(property, Predicate.isEqual(value));
        return this;
    }

    /**
     * Adds a property predicate
     *
     * @param property the property name
     * @param value the property value
     *
     * @return the search for build chaining
     */
    public Search forProperty(String property, Predicate<KDLValue> value) {
        properties.put(Predicate.isEqual(property), value);
        return this;
    }

    /**
     * Adds a property predicate
     *
     * @param property the property name
     * @param value the property value
     *
     * @return the search for build chaining
     */
    public Search forProperty(Predicate<String> property, Predicate<KDLValue> value) {
        properties.put(property, value);
        return this;
    }

    /**
     * Adds an argument predicate
     *
     * @param arg the argument
     * @return the search for build chaining
     */
    public Search forArg(KDLValue arg) {
        args.add(Predicate.isEqual(arg));
        return this;
    }

    /**
     * Adds an argument predicate
     *
     * @param arg the argument
     * @return the search for build chaining
     */
    public Search forArg(Predicate<KDLValue> arg) {
        args.add(arg);
        return this;
    }

    /**
     * Set the minimum depth in the tree where nodes may match the provided predicates. A value of 0 indicates the root.
     *
     * @param minDepth the minimum depth
     * @return the search for build chaining
     */
    public Search setMinDepth(int minDepth) {
        this.minDepth = minDepth;
        return this;
    }

    /**
     * Set the maximum depth in the tree where nodes may match the provided predicates. A value of 0 indicates the root.
     *
     * @param maxDepth the maximum depth
     * @return the search for build chaining
     */
    public Search setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    /**
     * Perform the search, returning a list of all matching nodes
     *
     * @return the list of matching nodes
     */
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

    /**
     * Return a copy of the document where only nodes matching the provided predicates remain. Note the given a document like:
     *
     * node_a {
     *     node_b
     * }
     *
     * And the search:
     *
     * KDLDocument filtered = document.search()
     *         .identifier("node_b")
     *         .filter
     *
     * An empty document will be returned since not all nodes in the path to the matching node themselves matched.
     *
     * @return the filtered document
     */
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

    /**
     * Applies a provided function to all matching nodes in the tree, returning a copy of the full tree with modified nodes
     *
     * @param fun the function to apply
     * @return A copy of the document with matching nodes mutated
     */
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
}
