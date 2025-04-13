# KDL4j v2

A Java implementation of a parser for the [KDL Document Language](https://github.com/kdl-org/kdl). Supports KDL
version `2.0.0`.

This library targets Java 11 as a minimum version.

## Status

![Gradle CI](https://github.com/kdl-org/kdl4j/workflows/Gradle%20CI/badge.svg)

## Usage

### Dependency

Releases are published on [GitHub Packages](https://docs.github.com/en/packages). You need to authenticate with GitHub
using a token with `read:packages` permission. See the official documentation for more information on how to
authenticate on GitHub Packages for
[Maven](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)
or for
[Gradle](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry).

Then you can add the KDL4j dependency. Maven:

```xml

<dependencies>
	<dependency>
		<groupId>dev.kdl</groupId>
		<artifactId>kdl4j</artifactId>
		<version>1.0.0-SNAPSHOT</version>
	</dependency>
</dependencies>
```

Gradle:

```groovy
dependencies {
	implementation 'dev.kdl:kdl4j:1.0.0-SNAPSHOT'
}
```

Alternatively, you can use the packages [hosted by JitPack](https://jitpack.io/#kdl-org/kdl4j). In this case, make sure
you use the `com.github.kdl-org` groupId.

### Parsing

```java
import parse.dev.kdl.KDLParser;

// Create a KDL 2 parser
var parser = KdlParser.v2();
// Parse from a String
var documentFromString = parser.parse("filename", "node_name \"arg\"");
// Parse from an InputStream
var documentFromReader = parser.parse("filename", new ByteArrayInputStream(/* … */));
// Parse from a file
var documentFromReader = parser.parse(Paths.get("path", "to", "file"));
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
[the test cases directory](src/test/resources/test-cases) to see the
existing ones.
See the README there for more details.
