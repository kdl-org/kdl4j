grammar kdl;

@header {
package dev.cbeck.kdl.antlr;
}

nodes : linespace* node nodes? linespace*;
node : (COMMENTED_CHUNK ws*)? identifier (node_space node_props_and_args)* (node_space* node_children ws*)? NODE_TERMINATOR;
node_props_and_args : (COMMENTED_CHUNK ws*)? (prop | value);
node_children : (COMMENTED_CHUNK ws*)? '{' nodes '}';
node_space : (ws* escline ws*) | ws+;
prop : identifier '=' value;

identifier : string | bare_identifier;
bare_identifier : BARE_ID;

value : string | number | BOOLEAN | NULL;

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
fragment OCTAL_DIGIT : [0-7] ;
fragment SIGN : ('+' | '-') ;
fragment F_NEWLINE : [\u000D\u000A\u0085\u000C\u2028\u2029] | '\u000D\u000A';
fragment INTEGER : SIGN? [0-9] [0-9_]* ;
fragment EXPONENT : ('e' | 'E') INTEGER;

COMMENTED_CHUNK : '/-';

fragment IDENTIFIER_CHAR : ~[\u000A\u000C\u000D\u0085\u2028\u2029\\{}<>;[\]=,"\p{White_Space}];
fragment IDENTIFIER_CHAR_MINUS_DIGIT : ~[\u000A\u000C\u000D\u0085\u2028\u2029\\{}<>;[\]=,"\p{White_Space}0-9];
BARE_ID : IDENTIFIER_CHAR_MINUS_DIGIT IDENTIFIER_CHAR*;

ANY_CHARACTER : .;
CHARACTER : ('\\' ESCAPE) | [^\\"];
DECIMAL : INTEGER ('.' [0-9]+)? EXPONENT?;
ESCAPE : (["\\/bfnrt] | 'u{' (HEX_DIGIT | HEX_DIGIT HEX_DIGIT | HEX_DIGIT HEX_DIGIT HEX_DIGIT | HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT | HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT | HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT) '}');

COMMENTED_BLOCK : '/*' ~[*/]* '*/';
HEX : '0x' HEX_DIGIT (HEX_DIGIT | '_')*;
OCTAL : '0o' OCTAL_DIGIT (OCTAL_DIGIT | '_')* ;
BINARY : '0b' ('0' | '1') ('0' | '1' | '_')* ;
BOM : '\uFFEF' ;
BOOLEAN : 'true' | 'false' ;
NULL : 'null';
NODE_TERMINATOR : SINGLE_LINE_COMMENT | F_NEWLINE | ';' | EOF;
SINGLE_LINE_COMMENT : '//' (~[\u000D\u000A\u0085\u000C\u2028\u2029])+ F_NEWLINE;
NEWLINE : F_NEWLINE;
UNICODE_WHITESPACE : [\p{White_Space}];
