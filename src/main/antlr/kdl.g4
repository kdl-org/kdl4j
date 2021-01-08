grammar kdl;

@header {
package dev.cbeck.kdl.antlr;
}

parse : nodes EOF;
nodes : LINESPACE* (node nodes?)? LINESPACE*;
node : (COMMENTED_CHUNK (WS | multi_line_comment)*)? identifier (NODE_SPACE node_props_and_args)* (NODE_SPACE? node_children (WS | multi_line_comment)*)? NODE_TERMINATOR;
node_props_and_args : (COMMENTED_CHUNK (WS | multi_line_comment)*)? (prop | value);
prop : identifier '=' value;
node_children : (COMMENTED_CHUNK (WS | multi_line_comment)*)? '{' nodes '}';

multi_line_comment : '/*' (COMMENTED_BLOCK | multi_line_comment) '*/';

value : BOOLEAN | number | NULL | string ;

string : raw_string | escaped_string;
escaped_string : CHARACTERS;
raw_string : 'r' raw_string_hash;
raw_string_hash : '#' raw_string_hash '#' | raw_string_quotes;
raw_string_quotes : ANY_CHARACTERS ;

identifier : string | bare_identifier;
bare_identifier : BARE_ID;

number : DECIMAL | HEX | OCTAL | BINARY;

NODE_SPACE : ((WS* ESCLINE WS*) | WS+) -> skip;
ESCLINE : '\\' WS* (SINGLE_LINE_COMMENT | NEWLINE);
LINESPACE : (NEWLINE | WS | SINGLE_LINE_COMMENT);
WS : (BOM | UNICODE_WHITESPACE);
COMMENTED_BLOCK : ('*' (~[/] | ~[*]))*;
COMMENTED_CHUNK : '/-';
ESCAPE : ["\\/bfnrt] | ('u{' (HEX_DIGIT | HEX_DIGIT HEX_DIGIT | HEX_DIGIT HEX_DIGIT HEX_DIGIT | HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT | HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT | HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT) '}');

DECIMAL : INTEGER ('.' [0-9]+)? EXPONENT?;
HEX : '0x' HEX_DIGIT (HEX_DIGIT | '_')*;
OCTAL : '0o' OCTAL_DIGIT (OCTAL_DIGIT | '_')* ;
BINARY : '0b' ('0' | '1') ('0' | '1' | '_')* ;
BOOLEAN : 'true' | 'false' ;
NULL : 'null';

UNICODE_WHITESPACE : [\u0009\u0020\u00A0\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u202F\u205F\u3000];
NODE_TERMINATOR : (F_NEWLINE | SINGLE_LINE_COMMENT | ';') -> skip;
SINGLE_LINE_COMMENT : '//' (~[\u000D\u000A\u0085\u000C\u2028\u2029])+ F_NEWLINE -> skip;
NEWLINE : F_NEWLINE -> skip;
CHARACTERS : '"' (~[\\"] | ('\\' ESCAPE))* '"' ;
BOM : '\uFFEF' -> skip;
ANY_CHARACTERS : '"' .*? '"';
BARE_ID : IDENTIFIER_CHAR_MINUS_DIGIT IDENTIFIER_CHAR*;

fragment HEX_DIGIT : [0-9a-fA-F] ;
fragment OCTAL_DIGIT : [0-7] ;
fragment SIGN : '+' | '-' ;
fragment F_NEWLINE : [\u000D\u000A\u0085\u000C\u2028\u2029] | '\u000D\u000A';
fragment INTEGER : SIGN? [0-9] [0-9_]* ;
fragment EXPONENT : ('e' | 'E') INTEGER;
fragment IDENTIFIER_CHAR : ~[\u000A\u000C\u000D\u0085\u2028\u2029\\{}<>;[\]=,"\u0009\u0020\u00A0\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u202F\u205F\u3000];
fragment IDENTIFIER_CHAR_MINUS_DIGIT : ~[\u000A\u000C\u000D\u0085\u2028\u2029\\{}<>;[\]=,"\u0009\u0020\u00A0\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u202F\u205F\u30000-9];
