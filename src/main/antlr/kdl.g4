grammar kdl;

@header {
package dev.cbeck.kdl.antlr;
}

nodes : linespace* node nodes? linespace*;
node : (COMMENTED_NODE ws*)? identifier (node_space node_props_and_args)* (node_space* node_children ws*)? NODE_TERMINATOR;
node_props_and_args : (COMMENTED_NODE ws*)? (prop | value);
node_children : (COMMENTED_NODE ws*)? '{' nodes '}';
node_space : ws* escline ws* | ws+;
prop : identifier '=' value;

identifier : string | bare_identifier;
bare_identifier : IDENTIFIER_CHAR_MINUS_DIGIT IDENTIFIER_CHAR*;


value : string | number | BOOLEAN | 'null';

string : raw_string | escaped_string;
escaped_string : '"' CHARACTER* '"';
raw_string : 'r' raw_string_hash;
raw_string_hash : '#' raw_string_hash '#' | raw_string_quotes;
raw_string_quotes : '"' ANY_CHARACTER* '"';

number : DECIMAL | HEX | OCTAL | BINARY;

escline : '\\' ws* (SINGLE_LINE_COMMENT | NEWLINE);
linespace : NEWLINE | ws | SINGLE_LINE_COMMENT;
ws : BOM | UNICODE_WHITESPACE | multi_line_comment;
multi_line_comment : '/*' (COMMENTED_BLOCK | multi_line_comment) '*/';

fragment HEX_DIGIT : [0-9a-fA-F] ;
fragment SIGN : ('+' | '-') ;
fragment F_UNICODE_WHITESPACE : ('\u0009' | '\u0020' | '\u00A0' | '\u1680' | '\u2000' | '\u2001' | '\u2002' | '\u2003' | '\u2004' | '\u2005' | '\u2006' | '\u2007' | '\u2008' | '\u2009' | '\u200A' | '\u202F' | '\u205F' | '\u3000') ;
fragment F_NEWLINE : ('\u000D' | '\u000A' | '\u000D\u000A' | '\u0085' | '\u000C' | '\u2028' | '\u2029') ;
fragment INTEGER : SIGN? [0-9] [0-9_]* ;
fragment EXPONENT : ('e' | 'E') INTEGER;

COMMENTED_NODE : '/-';
IDENTIFIER_CHAR : [^\u000A\u000C\u000D\u0085\u2028\u2029\\{}<>;[\]=,"];
IDENTIFIER_CHAR_MINUS_DIGIT : [^\u000A\u000C\u000D\u0085\u2028\u2029\\{}<>;[\]=,"0-9];
ANY_CHARACTER : .;
CHARACTER : ('\\' ESCAPE) | [^\\"];
DECIMAL : INTEGER ('.' [0_9]+)? EXPONENT?;
ESCAPE : (["\\/bfnrt] | 'u{' (HEX_DIGIT | HEX_DIGIT HEX_DIGIT | HEX_DIGIT HEX_DIGIT HEX_DIGIT | HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT | HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT | HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT) '}');

COMMENTED_BLOCK : ('*' [^\\/] | [^*]) ('*' [^\\/] | [^*])*;
HEX : '0x' HEX_DIGIT (HEX_DIGIT | '_')*;
OCTAL : '0o' [0-7] [0-7_]* ;
BINARY : '0b' ('0' | '1') ('0' | '1' | '_')* ;
BOM : '\uFFEF' ;
BOOLEAN : 'true' | 'false' ;
NODE_TERMINATOR : SINGLE_LINE_COMMENT | F_NEWLINE | ';' | EOF;
SINGLE_LINE_COMMENT : '//' [^\u000D\u000A\u0085\u000C\u2028\u2029]+ F_NEWLINE;
NEWLINE : F_NEWLINE;
UNICODE_WHITESPACE : F_UNICODE_WHITESPACE;
