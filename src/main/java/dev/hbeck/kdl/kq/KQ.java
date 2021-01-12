package dev.hbeck.kdl.kq;

/*
 * search := (('*' node_predicate) | ('{}' node predicate) |  ('.' node_predicates)) mutation?
 *
 * node_predicates := node_predicate ( '.' node_predicates)?
 * node_predicate := predicate_or_literal? ( '[' [|&]? prop_or_arg_predicates ']' )?
 * prop_or_arg_predicate := ( prop_predicate | arg_predicate ) ( ',' prop_or_arg_predicate )?
 * prop_predicate := predicate_or_literal [=><] predicate_or_literal
 * arg_predicate := [=<>]predicate_or_literal
 *
 * predicate_or_literal := identifier | regex
 * regex = '/' character* '/'
 *
 * mutation := add_op | subtract_op | set_op
 * add_op := '+' (string | ( string '=' string ) | '{' node* '}' )+
 * subtract_op := '-' (string '*'? | string '=')+
 * set_op := '=' (( string '=' string ) | '{' node* '}' )+
 *
 * -f/--file printConfig args
 *
 *
 * Examples:
 *     Pretty print full doc: `.`
 *
 *     Trim to branches from root with:
 *         Identifier 'mynode': `.mynode`
 *         An id starting with 'my': `./^my.*$/`
 *         With children: `.[{}]`
 *         With an arg "myarg": `.["myarg"]`
 *         With a prop "myprop" and value 10: `.[myprop=10]`
 *         With children and an arg starting with 'my': .[/^my.*$/ & {}]
 *         With a node one level deep with identifier 'myinnernode': `..myinnernode`
 *         With an arg greater than 10: `.[>10]`
 *
 *     Make modifications to the document, returning the full document:
 *         Add a node to the root: `{} + {mynode "a" "b"}`
 *         Remove all nodes from root named 'mynode' with an arg 'myarg': .mynode["myarg"] - .
 *         Set the value for properties whose key matches
 *
 */

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.parse.KDLParseException;
import dev.hbeck.kdl.parse.KDLParser;
import dev.hbeck.kdl.search.Operation;
import dev.hbeck.kdl.search.OperationParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class KQ {
    private static final OperationParser operationParser = new OperationParser();
    private static final KDLParser documentParser = new KDLParser();

    public static void main(String[] args) {
        final Operation operation;
        try {
            operation = operationParser.parse(String.join(" ", args));
        } catch (Throwable t) {
            System.err.printf("Couldn't parse operation: %s%n", t.getLocalizedMessage());
            System.exit(1);
            return;
        }

        final KDLDocument document;
        try {
            document = documentParser.parse(new InputStreamReader(System.in));
        } catch (KDLParseException e) {
            System.err.printf("Error reading document stream: %s%n", e.getLocalizedMessage());
            System.exit(2);
            return;
        } catch (IOException e) {
            System.err.printf("Parse error: %s%n", e.getLocalizedMessage());
            System.exit(3);
            return;
        }

        final KDLDocument result = document.apply(operation);
        try {
            result.writeKDLPretty(new OutputStreamWriter(System.out));
        } catch (IOException e) {
            System.err.printf("Error writing result: %s%n", e.getLocalizedMessage());
            System.exit(4);
        }
    }
}
