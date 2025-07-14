# Kdl4J

A Java implementation of a parser for the [KDL Document Language](https://github.com/kdl-org/kdl).
Supports both KDL v1 and KDL v2 syntaxes.

This library targets Java 17 as a minimum version.

![Build Workflow Badge](https://github.com/kdl-org/kdl4j/workflows/Build%20Workflow/badge.svg)

## Usage

### Dependency

Releases are published on [GitHub Packages](https://docs.github.com/en/packages). You need to
authenticate with GitHub using a token with `read:packages` permission. See the official
documentation for more information on how to authenticate on GitHub Packages
for[Maven](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)
or
for[Gradle](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry).

Then you can add the Kdl4J dependency.

Maven:

```xml
<dependency>
	<groupId>dev.kdl</groupId>
	<artifactId>kdl4j</artifactId>
	<version>1.0.0-RC3</version>
</dependency>
```

Gradle:

```groovy
implementation 'dev.kdl:kdl4j:1.0.0-RC3'
```

Alternatively, you can use the packages [hosted by JitPack](https://jitpack.io/#kdl-org/kdl4j). In
this case, make sure you use the `com.github.kdl-org` groupId.

### Parsing

The `KdlParser` class can create a parser for KDL v1 or KDL v2 syntax. It can also create a _hybrid_
parser that first tries to parse using v2 syntax but switches to v1 syntax if parsing fails.

Parsers are thread-safe.

```java
// Create a KDL 2 parser
var parser = KdlParser.v2();
// Parse from a String
var documentFromString = parser.parse("node_name \"arg\"");
// Parse from an InputStream
var documentFromReader = parser.parse(new ByteArrayInputStream(/* … */));
// Parse from a file
var documentFromReader = parser.parse(Paths.get("path", "to", "file"));
```

#### Displaying parsing errors to users

The `Reporter` class can display an error message from a `KdlParseException`, which is thrown when a
document is invalid. The report can optionally include ANSI escape codes for color.

Example:

```
× Number or identifier cannot start with '.':
  ╭─[test.kdl:1:6]
1 │ node .0n
  ·      ┬
  ·      ╰ invalid character
  ╰─
help: for a number add a zero before '.', for an identifier use quotes
```

### Printing

The `KdlPrinter` class allows printing a KDL document to a `String`, a `Writer`, an `OutputStream`
or to a file. By default, it:

- uses KDL 2.0 syntax
- prints one character tabulation for each indentation level
- uses _line feed_ as the newline character
- does not print node separators (`;`)
- does not print braces for nodes without children
- prints arguments and properties with null value
- prints all duplicate properties
- prints properties in the declaration order
- uses `E` as the exponent character in decimal values
- does not print quotes around identifiers

Any of these can be changed by creating a `PrintConfiguration` and passing it to the `KDLPrinter`
constructor.

## Contributing

Please read the Code of Conduct before opening any issues or pull requests. The easiest way to help
is by writing documentation or test cases, but all contributions are welcome.
