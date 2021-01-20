package dev.hbeck.kdl.kq

import dev.hbeck.kdl.objects.KDLDocument
import dev.hbeck.kdl.parse.KDLParser
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


@RunWith(Parameterized::class)
class TestFullOperationParser(private val input: String, private val query: String, private val expected: String) {
    private val docParser = KDLParser()
    private val opParser = OperationParser()

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} -> {1} -> {2}")
        fun getCases(): List<Array<Any>> = listOf(
                arrayOf("", ".", ""),
                arrayOf("node1", ".", "node1"),
                arrayOf("node1; node2", ".", "node1; node2"),
                arrayOf("node1; node2 {node3;}", ".", "node1; node2 {node3;}"),
                arrayOf("node1; node2", ".node1", "node1"),
                arrayOf("node1; node2", ".idk", ""),
                arrayOf("node1; node2; node3", "./node[12]/", "node1; node2"),
                arrayOf("node1; node2; node3", ".r/node[12]/", "node1; node2"),
                arrayOf("node1; node2; node3", ".r##/node[12]/##", "node1; node2"),
                arrayOf("node1; node2 {node3;}", ".node2.node3", "node2 {node3;}"),
                arrayOf("node 1; node 2", ".node[1]", "node 1"),
                arrayOf("node a=1; node a=2", ".node[a=1]", "node a=1"),
                arrayOf("node 1 {node 2;}; node 1 {node 1;}", ".node[1].node[1]", "node 1 {node 1;}"),
                arrayOf("node 1; node 2; node 3", ".node[1 | 2]", "node 1; node 2"),
                arrayOf("node 1 2; node 2; node 1", ".node[1 & 2]", "node 1 2"),
                arrayOf("node 1 2 3; node 1 2; node 2 3", ".node[1 & 2 & 3]", "node 1 2 3"),
                arrayOf("node key=10", ".[key=10]", "node key=10"),
                arrayOf("node key=10", ".[key>10]", ""),
                arrayOf("node key=11", ".[key>10]", "node key=11"),
                arrayOf("node key=9.5", ".[key<10]", "node key=9.5"),
                arrayOf("node key=10", ".[key=0x0a]", "node key=10"),
                arrayOf("node key=\"val\"", ".node[/^k/~/^v/]", "node key=\"val\""),
                arrayOf("node", ".[{}]", "node"),
                arrayOf("node {}", ".[{}]", "node {}"),
                arrayOf("node1; node2 {node3;}", ".[{}]", "node1"),
                arrayOf("node1; node2 {node3;}", ".[{.}]", "node2 {node3;}"),
                arrayOf("node1; node2 {node3;}", ".[{*}]", "node2 {node3;}"),
                arrayOf("node1 {node2 {node3;};}; node1 {node2;}", ".node1.node2.node3", "node1 {node2 {node3;};}"),
                arrayOf("node1 key=1; node2 key=2; node3 key=4", ".[key<3]", "node1 key=1; node2 key=2"),
                arrayOf("node1 key=1; node2 key=2; node3 key=4; node4 key=6", ".[key<3 | key>5 ]", "node1 key=1; node2 key=2; node4 key=6"),
                arrayOf("node1 1; node2 1; node3 2", ".[!<2]", "node3 2"),
                arrayOf("node1 {node2;}; node3 key=3; node4 key=4", "./^node/[{*} | key=3]", "node1 {node2;}; node3 key=3"),
                arrayOf("node1 {node2 {node3;};}; node4", ".r/^node/[{.node2.node3}]", "node1 {node2 {node3;};}"),
                arrayOf("node key=true", ".[key=*]", "node key=true"),
                arrayOf("node1 key=null; node2 key=true", "./^node/[key=*]", "node2 key=true"),
                arrayOf("node1 1 true; node2 2 false; node3 3 true", ".[.[0]=1 | (.[0]=3 & true)]", "node1 1 true; node3 3 true"),
                arrayOf("node1 1 true; node2 2 false; node3 3 true", ".[.[0]=1 | ((.[0]=3 & true))]", "node1 1 true; node3 3 true"),
                arrayOf("node1 1 true; node2 2 false; node3 3 true", ".[(.[0]=1) | (.[0]=3 & true)]", "node1 1 true; node3 3 true"),
                arrayOf("node1 0; node2 1", ".[.[0]=1]", "node2 1"),
                arrayOf("node1", ". + 1", "node1 1"),
                arrayOf("node1", ". + key=true", "node1 key=true"),
                arrayOf("node1", ". + {}", "node1 {}"),
                arrayOf("node1", ". + {node2;}", "node1 {node2;}"),
                arrayOf("node1", ". + .[0]=1", "node1 1"),
                arrayOf("node1 2", ". + .[0]=1", "node1 1 2"),
                arrayOf("node1", ". + 1 2 3", "node1 1 2 3"),
                arrayOf("node1 1", ". + .[0]=0 2 3", "node1 0 1 2 3"),
                arrayOf("node1", ". + key=true key=false", "node1 key=false"),
                arrayOf("node1 1", ". - 1", "node1"),
                arrayOf("node1; node2", ".node1 - .", "node2"),
                arrayOf("node1 {node2 {node3;}; node4;}", ".node1.node2 - .", "node1 {node4;}"),
                arrayOf("node1 {node2 {node3;}; node4;}", ".node1.node2 - {*}", "node1 {node2 {}; node4;}"),
                arrayOf("node1 1; node2 key=1", ". - 1", "node1; node2 key=1"),
                arrayOf("node1 1 2", ". - .[0] .[0]", "node1 2"),
                arrayOf("node1; node2", ". - [*]", "node1; node2"),
                arrayOf("node1 1; node2 key=false", ". - [*]", "node1; node2"),
                arrayOf("node1 null key=true; node2 null key=false", ". - key=*", "node1 null; node2 null"),
                arrayOf("", ".", ""),
                arrayOf("", ".", ""),
                arrayOf("", ".", ""),
                arrayOf("", ".", ""),

                arrayOf("", "{}", ""),
                arrayOf("node", "{}", "node"),
                arrayOf("node1; node2", "{}", "node1; node2"),
                arrayOf("", "{} + {}", ""),
                arrayOf("node1", "{} + {}", "node1"),
                arrayOf("", "{} + {node1;}", "node1"),
                arrayOf("node1", "{} + {node2 1;}", "node1; node2 1"),
        )
    }

    @Test
    fun test() {
        val inputDoc = docParser.parse(input)
        val expectedDoc = docParser.parse(expected)
        val (search, mutation) = opParser.parse(query)
        val output = mutation?.let {search.mutate(inputDoc, it)} ?: search.filter(inputDoc, false)

        assertThat(expectedGot(expectedDoc, output), output, equalTo(expectedDoc))
    }

    fun expectedGot(expected: KDLDocument, got: KDLDocument): String =
            "Expected:\n${expected.toKDLPretty()}\n---\nGot:\n${got.toKDLPretty()}"
}