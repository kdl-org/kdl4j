package dev.hbeck.kdl.kq;

/*
 * search := '.' node_predicates mutation?
 *
 * node_predicates := node_predicate ( '.' node_predicates)?
 * node_predicate := predicate_or_literal? ( '[' prop_or_arg_predicates ']' )?
 * prop_or_arg_predicate := ( prop_predicate | arg_predicate ) ( ',' prop_or_arg_predicate )?
 * prop_predicate := predicate_or_literal '=' predicate_or_literal
 * arg_predicate := predicate_or_literal
 *
 * predicate_or_literal := identifier | regex
 * regex = '/' character* '/'
 *
 * mutation := add_op | subtract_op | set_op
 * add_op := '+' (string | ( string '=' string ) | '{' node* '}' )+
 * subtract_op := '-' (string '*'? | string '=')+
 * set_op := '=' (( string '=' string ) | '{' node* '}' )+
 *
 * --all-props --all-args -f/--file
 *
 */



public class KQ {
}
