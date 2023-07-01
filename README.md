# KDL4j

A Java implementation of a parser for the [KDL Document Language](https://github.com/kdl-org/kdl).

## Status

![Gradle CI](https://github.com/hkolbeck/kdl4j/workflows/Gradle%20CI/badge.svg)

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

### Searching and Mutating Documents

Several utilities are provided for finding nodes in documents. Each presents the same interface, but the way they search
the document differs. There are three search types:

* RootSearch - Searches entirely at the root, primarily used for mutations to the root as discussed below
* GeneralSearch - Searches for nodes anywhere in the tree matching a single, possibly compound, node predicate
* PathedSearch - Searches for nodes down a specified path. At each level a different node predicate can be specified

Each provides four methods for searching or mutating documents:

* `anyMatch(document)` - Returns true if any node matches the search, false otherwise
* `filter(document, trim)` - Removes all nodes from the tree not on a branch that matches the predicates of the search. if
  `trim` is set, removes all their non-matching children
* `list(document, trim)` - Produces a new document with all matching nodes at the root. If `trim` is set, removes all
  their non-matching children
* `mutate(document, mutation)` - Applies a provided `Mutation` to every matching node in the tree, depth first.

There are 3 types of `Mutations` provided, and users may provide custom mutations. Provided are `AddMutation`, 
`SubtractMutation`, and `SetMutation`. Each performs functions hinted at by the name. See individual javadocs for details.

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
