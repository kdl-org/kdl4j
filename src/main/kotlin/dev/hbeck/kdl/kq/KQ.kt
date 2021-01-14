package dev.hbeck.kdl.kq

import dev.hbeck.kdl.parse.KDLParser
import kotlin.jvm.JvmStatic
import dev.hbeck.kdl.objects.KDLDocument
import java.io.InputStreamReader
import dev.hbeck.kdl.parse.KDLParseException
import dev.hbeck.kdl.search.Operation
import java.io.IOException
import java.io.OutputStreamWriter

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
 * ---


 * ---
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
object KQ {
    private val operationParser = OperationParser()
    private val documentParser = KDLParser()

    @JvmStatic
    fun main(args: Array<String>) {
        val operation: Operation = try {
            operationParser.parse(args.joinToString(" "))
        } catch (t: Throwable) {
            System.err.printf("Couldn't parse operation: %s%n", t.localizedMessage)
            System.exit(1)
            return
        }

        val document: KDLDocument = try {
            documentParser.parse(InputStreamReader(System.`in`))
        } catch (e: KDLParseException) {
            System.err.printf("Error reading document stream: %s%n", e.localizedMessage)
            System.exit(2)
            return
        } catch (e: IOException) {
            System.err.printf("Parse error: %s%n", e.localizedMessage)
            System.exit(3)
            return
        }

        val result = document.apply(operation)
        try {
            result.writeKDLPretty(OutputStreamWriter(System.out))
        } catch (e: IOException) {
            System.err.printf("Error writing result: %s%n", e.localizedMessage)
            System.exit(4)
        }
    }
}