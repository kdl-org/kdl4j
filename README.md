# KDL4j v2

A Java implementation of a parser for the [KDL Document Language](https://github.com/kdl-org/kdl). Supports KDL
version `2.0.0-draft.4`.

This library targets Java 11 as a minimum version.

## Status

![Gradle CI](https://github.com/hkolbeck/kdl4j/workflows/Gradle%20CI/badge.svg)

## Usage

### Parsing

```java
import kdl.parse.KDLParser;

// Parse from a String
var documentFromString = KDLParser.parse("node_name \"arg\"");
// Parse from an InputStream
var documentFromReader = KDLParser.parse(new ByteArrayInputStream(/* … */));
// Parse from a file
var documentFromReader = KDLParser.parse(Paths.get("path", "to", "file"));
```

### Printing

The `KDLPrinter` class allows printing a KDL document to a `String`, a `Writer`, an `OutputStream` or to a file. By
default, it:

- prints one character tabulation for each indentation level
- does not print node separators (`;`)
- does not print braces for nodes without children
- prints arguments and properties with null value
- uses `E` as the exponent character in decimal values

Any of these can be changed by creating a `PrintConfiguration` and passing it to the `KDLPrinter` constructor.

## Contributing

Please read the Code of Conduct before opening any issues or pull requests.

Besides code fixes, the easiest way to contribute is by generating test cases. Check out
[the test cases directory](src/test/resources/test_cases) to see the
existing ones.
See the README there for more details.
