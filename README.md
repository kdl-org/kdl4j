# KDL4j

A Java implementation of a parser for the [KDL Document Language](https://github.com/kdl-org/kdl).

## Status

This project is alpha-quality. While it's well tested, there are almost certainly bugs still lurking.

## Usage

```java
final KDLParser parser = new KDLParser();

final KDLDocument documentFromString = parser.parse("node_name \"arg\"")
// OR
final KDLDocument documentFromReader = parser.parse(new FileReader("some/file.kdl"))
```



## Contributing

Please read the Code of Conduct before opening any issues or pull requests.

Besides code fixes, the easiest way to contribute is by generating test cases. Check out 
[the test cases directory](https://github.com/hkolbeck/kdl4j/tree/trunk/src/test/resources/test_cases) to see the existing ones.
To add a test case, add a `.kdl` file to the `input` directory and a file with the same name to the `expected_kdl` directory.
The expected file should have all comments and extra newlines removed except for a newline after the last node, and be 
formatted with 4 space indents. To try out your test cases, run the `TestRoundTrip` test class.

## TODO

* More tests
* Switch from Sets to switch statements for character classes
* Show full line on error
* Convenience methods on model objects