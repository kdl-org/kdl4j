# KDLQL: KDL Query Language

Any document language needs a simple way of making alterations. KDLQL is largely based on the query language implemented
by the UNIX `jq` tool for working with JSON. There are however major modifications to match KDL's differing semantics.

## Basic Operation

Each query is a `Search`, followed by an optional `Mutation`. Searches can start with:

* `{}` - Root Search: Operates purely on the root document
* `*` - General Search: Searches anywhere in the document
* `.` - Pathed Search: Searches for nodes on a specified path

The optional `Mutation` can make one of three alteration types to the document:

* `=` - Set Mutation: Set the identifier of a node, set or add property values, set the argument list of a node, set the
  child of a node
* `-` - Subtraction Mutation: Removes properties based on their key and optionally value matching predicates, removes 
  args matching predicates, removes all args and properties, remove the child of a node, or removes the whole node
* `+` - Addition Mutation: Adds properties, arguments, children, or nodes to existing children

### Atom Predicates

#### String Predicates

String predicates can be applied to node identifiers, string values and arguments, and property keys. If the value being
matched is anything but a string, the match will fail. String predicates come in two flavors:

* Literals: Matches strings whose text is exactly the same as the specified predicate. Can be bare strings in the case of 
  identifiers and property keys, escaped, or raw. Note that this comparison ignores whether the string was specified using
  raw or escaped strings. Comparisons are case-sensitive.
* Regular Expressions: Similar to strings, regexes can be escaped or raw. Escaped regexes are surrounded by `/` characters,
  while raw regexes are preceeded by an `r`, then any number of `#`, then a `/` and followed by a `/`, and then the same 
  number of `#`. Regular expressions use Java's `Pattern` syntax outlined 
  [here](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).
  

#### Numeric Predicates
  
Numeric predicates match only numbers, and ignore radix. They consist of a comparison operator followed immediately by
a `number` as defined in the KDL specification. They can be used as part of a property predicate as outlined below, or
alone. If they are alone they match if any numeric arg on the node matches. They are:

* `>10` - Greater than
* `<1.25E+100` - Less than
* `=0xFFFF` - Equal to


#### Literal Predicates

Literal predicates only support strict equality:

* `true`/`false`
* `null`


#### Property Predicates

Property predicates have two parts, one matching the key and the other the value. Both must match for the predicate to 
match. The key can be matched by either a string or a regex, while the value can be matched by any predicate type.

Examples:

* `mykey="myvalue` - Matches if key and value match exactly
* `/.*/="somevalue` - Matches any property with the string value `somevalue`
* `r"myrawkey">100` - Matches if a property exists with key `mykey` and a numeric value greater than 100
* `r/^\w+$/~/^\\w+$/` - Matches if both key and value are composed of nothing but "word" characters
* `mykey=*` - Matches if a property exists with the key `mykey` and any value of any type


#### Child Predicates

Child predicates match nodes based on the contents of their children:

* `{}` - Matches if the node has an empty or absent child
* `{ search }` - Search can either be Pathed or General. The node matches if the search finds any matching nodes in the
  child document

### Node Predicates

Node predicates combine atom predicates into a larger predicate that matches nodes. The basic format is:

```
identifier-predicate[ content-predicates ]
```

Where either part can be absent. If both are absent, the predicate matches any node. The identifier predicate can be
literal or a regex. Content predicates can either be:

* `[]` - Matches nodes with no arguments, properties, or child 
* `[*]` - Matches nodes that have any of properties, arguments, and children
* `[ predicate expression ]` - Matches nodes which match `predicate expression`


#### Predicate Expressions

Predicate expressions are composed of arg predicates, property predicates, and child predicates. They are joined in 
logical expressions using `&` for `and`, `|` for `or`, `!` for `not`, as well as `(` and `)` for grouping. There is no
operator precedence, so if `&` and `|` are mixed precedence must be specified with parens. For example: 
`[ "arg1" & >10 & (prop="val" | {*})]`


## Searches

### Root Search

Denoted by `{}`. Not really a search, as it takes no predicates. Instead, it allows operations on the root document.
The only mutation supported on root searches is additions with only a child.


### General Search

Denoted by `*` followed by an optional min/max depth specification `{minDepth, maxDepth}`, followed by an optional node 
predicate. Searches the document at least `minDepth` and at most `maxDepth` levels deep for nodes matching the predicate.
If a mutation is specified, it will be applied to each matching node, and the full document tree will be returned. If no
mutation is specified, the tree will be trimmed such that each leaf matches predicate and is at most `maxDepth` levels
deep.

Examples:

* `*` - Returns the full original document
* `*mynode` - Returns a tree with all nodes that are not either named `mynode` or on a branch between the root and a node
  named `mynode` removed (if no mutation is specified)
* `*{,4}mynode` - Return a tree with branches trimmed as above, but with the additional requirement that those nodes
  be at most 4 levels deep


### Pathed Search

Denoted by an arbitrarily long chain level predicates composed of `.` followed by an optional node predicate. If no node 
predicate is provided then any node matches. Each level predicate matches nodes at the corresponding level of the document
tree. Similar to the general search, any node that doesn't match the path predicate or lie along a branch to one matching
will be removed unless a mutation is specified. This search type is the most similar to `jq`.

Examples:

* `.` - Returns the root document with all children empty
* `...` - Returns the full document up to three levels deep
* `.mynode./^a.*/.["arg"]` - Matches branches where the first level node is named `mynode`, the second level's name starts
  with `a`, and the third level has an argument `"arg"`
  

## Mutations

If a mutation is specified, instead of trimming the tree to matching nodes, the specified mutation is applied to every
matching node.


### Set

Denoted by `<search> = <set item list>`. The set item list can contain at most one child specification, one identifier
specification, and an arbitrary number of arguments and properties. If any arguments are present then the argument list 
of the node will be cleared and replaced by the specified arguments, ordered left to right. If any properties are present
The set of properties will be cleared and replaced by any specified properties.

Examples:

* `. = {}` - Sets the child of every node at the root to empty
* `*mynode = =yournode` - Renames every node with the identifier `mynode` to `yournode`
* `*[prop=*] = prop2="val2"` - Clear the properties of every node with a property named `prop` and set the properties to
  `prop2="val2"`


### Addition

Denoted by `<search> + <add item list>`. The add item list can contain at most one child specification and any number of
properties and arguments. Added arguments are added to the right side of the argument list, and property updates are 
applied left to right. Any nodes in the child will be merged with the nodes in any existing child. Addition is the only
mutation supported by root searches.

Examples:

* `. + {subnode}` - Adds a node named `subnode` to the child of every node in the root document
* `*mynode + "arg"` - Adds an argument `"arg"` to every node in the tree with the identifier `mynode`
* `{} + {first-level-node "arg"; another-node "arg2"}` - Adds two new nodes to the root document

### Subtraction

Denoted `<search> - <sub item list>`. Removes arguments, properties, children, and entire nodes from the tree. Allows for
matching with predicates if exact argument or property values aren't known.

Examples:

* `* - /^a.*/` - Remove all arguments starting with `a` from every node in the tree
* `*to-be-deleted - .` - Removes every node named `to-be-deleted` from the tree
* `.node.node2 - [*]` - Removes all arguments and properties from nodes down the specified path
* `. - {*}` - Empties, but doesn't remove, all children of nodes at the root
* `. - {}` - Deletes the children of every node at the root
* `* - /^a.*/=*` - Removes any properties whose key starts with `a` from the tree regardless of their value
* `* - null /.*/=null` - Removes all null arguments and properties from every node in the tree


## Full Specification

```
operation :=  ( search ws* mutation? ) | ( root ws* ( '+' ws* child-nodes ) )

ws := unicode-whitespace
search := general-search | pathed-search
root := '{}'
general-search := '*' depth? node-predicate?
depth := '{' ws* number? ws* ',' ws* number? ws* '}' 
pathed-search := '.' node-predicate ws* pathed-search?

node-predicate := identifier-predicate? content-predicates?
identifier-predicate := bare-identifier | string | regex

content-predicates := '[' ws* ( content-predicate | '*' ) ws* ']'
content-predicate := ( '(' ws* compound-expr ws* ')' ) | atom --May omit parens around outermost
compound-expr := and-expr | or-expr
and-expr := content-predicate ws* '&' ws* content-predicate ( ws* '&' ws* content-predicate )*
or-expr := content-predicate ws* '|' ws* content-predicate ( ws* '|' ws* content-predicate )*
atom := '!'? ( child-predicate | prop-or-arg-predicate )

child-predicate := '{' ws* ( general-search | pathed-search ) ws* '}'
prop-or-arg-predicate := value | regex | numeric-predicate | property-predicate
property-predicate := identifier-predicate ( '=' value | '~' regex | numeric-predicate | '=*' )

numeric-predicate := ( '=' | '<' | '>' ) number

regex := escaped-regex | raw-regex
escaped-regex := '/' character* '/'

mutation := add-mutation | sub-mutation | set-mutation

add-mutation := ws* '+' ws* add-list? ( ws+ node-children )?
add-list := ( value | prop ) ( ws+ add-list )?

sub-mutation := ws* '-' ws* ( subtraction sub-list? | '.' ) 
subtraction := ( value | regex | numeric-predicate | property-predicate | '{' '*'? '}' |  '[*]' )
sub-list := ws+ subtraction sub-list?

set-mutation := ws* '=' ws* set-item set-list?
set-item := value | identifier '=' value | '=' identifier | node-children
set-list :=  set-item ( ws+ set-list )?
```
