# Full Document Test Cases

The `input` folder contains test cases for KDL parsers. See `src/test/java/dev/hbeck/kdl/TestRoundTrip.java`
for an example of how these documents are used for kdl4j. You're encouraged to use them for your project and/or add new ones
here. The `expected_kdl` folder contains files with the same name as those in `input` with the expected output after being 
run through the parser and printed out again. If there's no file in `expected_kdl` with a name corresponding to one in `input`
it indicates that parsing for that case should fail.

By necessity, the files in `expected_kdl` are not identical to their corresponding inputs. They are instead pretty-printed
using the default print configuration found in `src/main/java/dev/hbeck/kdl/print/PrintConfig.java`. This means that when
adding a test case the expected output must be structurally identical with:

* All comments removed
* Extra empty lines removed except for a newline after the last node
* All nodes should be reformatted without escaped newlines
* Node fields should be `identifier <args in same order> <properties in alpha order by key> <child if present>`
* All strings/identifiers should be at the lowest level of quoting possible. `r"words"` becomes `"words"` if a value or `words`
  if an identifier
* Any duplicate properties removed, with only the rightmost one remaining
* Any literal newlines or other ascii escape characters in escaped strings replaced with their escape sequences
* 4 space indents
* `E` used to indicate the exponent of all floating point literals

Note that unlike the tests in the core KDL repo, these *DO* expect numbers to be roundtripped maintaining their radix