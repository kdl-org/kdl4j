Kdl4J Changelog
===============

1.0.0
-----

### Fixed

- add missing overrides to `KdValue` implementors (@chmod222)


1.0.0
-----

This is the first release that supports KDL v2. ⚠️ It is not compatible with previous versions of
Kdl4J.

### Added

- new immutable KDL model classes, with builder for easier creation and modifications
- a KDL 2.0 parser
- a hybrid parser that first tries to parse the document using the KDL 2.0 parsers but falls back to
  the KDL 1.0 parser when it fails
- an error reporter to display user-friendly error messages, with optional ANSI escape codes for
  colors

### Modified

- the printer can now use the KDL 1.0 or KDL 2.0 syntax, depending on its configuration
