# KDL4j

A Java implementation of a parser for the [KDL Document Language](https://github.com/kdl-org/kdl).

## Status

This project is alpha-quality. While it's well tested, there are almost certainly bugs still lurking.

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

## Contributing

Please read the Code of Conduct before opening any issues or pull requests.

Besides code fixes, the easiest way to contribute is by generating test cases. Check out 
[the test cases directory](https://github.com/hkolbeck/kdl4j/tree/trunk/src/test/resources/test_cases) to see the existing ones.
To add a test case, add a `.kdl` file to the `input` directory and a file with the same name to the `expected_kdl` directory.
The expected file should have:

* All comments removed
* Extra empty lines removed except for a newline after the last node
* All nodes should be reformatted without escaped newlines 
* Node fields should be `identifier <args> <properties in alpha order by key> <child if present>`
* All strings/identifiers should be at the lowest level of quoting possible. `r"words"` becomes `"words"` if a value or `words` 
  if an identifier
* Any duplicate properties removed, with only the rightmost one remaining
* Replace any literal newlines or other ascii escape characters in escaped strings with their escape sequences

formatted with 4 space indents. To try out your test cases, run the `TestRoundTrip` test class.

## TODO

* More tests
