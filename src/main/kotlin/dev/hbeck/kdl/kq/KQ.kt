package dev.hbeck.kdl.kq

import dev.hbeck.kdl.parse.KDLParser
import kotlin.jvm.JvmStatic
import dev.hbeck.kdl.objects.KDLDocument
import java.io.InputStreamReader
import dev.hbeck.kdl.parse.KDLParseException
import java.io.IOException
import java.io.OutputStreamWriter
import java.lang.Exception
import kotlin.system.exitProcess

object KQ {
    private val operationParser = OperationParser()
    private val documentParser = KDLParser()

    @JvmStatic
    fun main(args: Array<String>) {
        val (search, mutation) = try {
            operationParser.parse(args.joinToString(" "))
        } catch (t: Throwable) {
            System.err.printf("Couldn't parse operation: %s%n", t.localizedMessage)
            exitProcess(1)
        }

        val document: KDLDocument = try {
            documentParser.parse(InputStreamReader(System.`in`))
        } catch (e: KDLParseException) {
            System.err.printf("Parse error: %s%n", e.localizedMessage)
            exitProcess(2)
        } catch (e: IOException) {
            System.err.printf("Error reading document stream: %s%n", e.localizedMessage)
            exitProcess(3)
        }

        val result = try {
            mutation?.let { search.mutate(document, mutation) } ?: search.filter(document, false)
        } catch (e: Exception) {
            System.err.println("Search or mutation failed: '${e.localizedMessage}'")
            exitProcess(4)
        }

        try {
            result.writeKDLPretty(OutputStreamWriter(System.out))
        } catch (e: IOException) {
            System.err.printf("Error writing result: %s%n", e.localizedMessage)
            exitProcess(5)
        }
    }
}