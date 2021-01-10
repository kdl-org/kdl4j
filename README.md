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

### Searching

The `Search` class allows for quick location of nodes in the document tree. Nodes match if all of below are true:

* Matches at least one identifier predicate
* Matches at least one argument predicate if specified, or all of them if `matchAllArgs()` is set
* Matches at least one property predicate if specified, or all of them if `matchAllProps()` is set
* Falls within the bounds of `minDepth` and `maxDepth` if either or both are set

```java
final List<KDLNode> matchingNodes = Search.of(document)
        .forNodeId("mynode") //Literal, can specify multiple
        .forNodeId(node -> nodeId.startsWith("my")) // Predicate    
        .forProperty("prop", KDLString.from("val")) // Either prop or val can also be predicates, can specify multiple
        .forArg(KDLBoolean.TRUE) // Can also be a predicate, can specify multiple
        .matchAllProps() // Otherwise, node will match if any props match as well as identifier
        .matchAllArgs() // Otherwise, node will match if any args match as well as identifier
        .setMinDepth(1) // 0 is the root document
        .setMaxDepth(1) // With the above, searches only the children of children of the root document
        .search(); // execute the search
```

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
  if an identifier.
* Any duplicate properties removed, with only the rightmost one remaining  

formatted with 4 space indents. To try out your test cases, run the `TestRoundTrip` test class.

## TODO

* More tests
* Switch from Sets to switch statements for character classes
* Show full line on error
* Convenience methods on model objects
