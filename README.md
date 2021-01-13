# KDL4j

A Java implementation of a parser for the [KDL Document Language](https://github.com/kdl-org/kdl).

## Status

This project is beta-quality. It's been extensively tested, but the spec it implements is still in flux.

## Usage

### Parsing

```java
final KDLParser parser = new KDLParser();

final KDLDocument documentFromString = parser.parse("node_name \"arg\"")
// OR
final KDLDocument documentFromReader = parser.parse(new FileReader("some/file.kdl"))
```

`KDLDocument` objects, and all descendants of `KDLObject`, are immutable and threadsafe, though that is not true of their 
`Builder` objects. If you need to make changes to a `KDLDocument`, use the `filter()` and `mutate()` functions explained below.

### Searching

The `Search` class allows for quick location of nodes in the document tree. Nodes match if all of the below are true:

* Matches at least one identifier predicate
* Matches at least one argument predicate if specified, or all of them if `matchAllArgs()` is set
* Matches at least one property predicate if specified, or all of them if `matchAllProps()` is set
* Falls within the bounds of `minDepth` and `maxDepth` if either or both are set

```java
final List<KDLNode> matchingNodes = document.search()
        .forNodeId("mynode") //Literal, can specify multiple
        .forNodeId(node -> nodeId.startsWith("my")) // Predicate    
        .forProperty("prop", KDLString.from("val")) // Either prop or val can also be predicates, can specify multiple
        .forArg(KDLBoolean.TRUE) // Can also be a predicate, can specify multiple
        .matchAllPropPredicates() // Otherwise, node will match if any props match as well as identifier
        .matchAllArgPredicates() // Otherwise, node will match if any args match as well as identifier
        .setMinDepth(1) // 0 is the root document
        .setMaxDepth(1) // With the above, searches only the children of children of the root document
        .search(); // execute the search
```

In a addition, the `Search` object exposes two more functions: `filter()` and `mutate(fun)`. These are called instead of
the final `search()`, and either remove all nodes not matching the specified predicates or allow mutation of all matching
nodes. Both return a new `KDLDocument`.

### Printing

By default, calling `document.toKDL()` or `document.writeKDL(writer)` will print the structure with:
 
* 4 space indents
* No semicolons
* Printable ASCII characters which can be escaped, escaped
* Empty children printed
* `null` arguments and properties with `null` values printed
* `\n` (unicode `\u{0a}`) for newlines

Any of these can be changed by creating a new PrintConfig object and passing it into the print method. See the javadocs
on PrintConfig for more information.

## Contributing

Please read the Code of Conduct before opening any issues or pull requests.

Besides code fixes, the easiest way to contribute is by generating test cases. Check out 
[the test cases directory](https://github.com/hkolbeck/kdl4j/tree/trunk/src/test/resources/test_cases) to see the existing ones.
See the README there for more details.
