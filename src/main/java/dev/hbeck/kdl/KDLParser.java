package dev.hbeck.kdl;

import dev.hbeck.kdl.antlr.kdlLexer;
import dev.hbeck.kdl.antlr.kdlParser;
import dev.hbeck.kdl.objects.KDLDocument;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class KDLParser {

    private static final String ex =
            "package {\n" +
            "    name \"kdl\"\n" +
            "    version \"0.0.0\"\n" +
            "    description \"kat's document language\"\n" +
            "    authors \"Kat Marchán <kzm@zkat.tech>\"\n" +
            "    license-file \"LICENSE.md\"\n" +
            "    edition \"2018\"\n" +
            "}\n" +
            "\n" +
            "dependencies {\n" +
            "    nom \"6.0.1\"\n" +
            "    thiserror \"1.0.22\"\n" +
            "}";

    public static void main(String[] args) throws IOException {
        final KDLParser parser = new KDLParser();
        final KDLDocument document = parser.parse(new StringReader(ex));
        
        System.out.println(document.toKDLPretty(4));
    }

    public KDLDocument parse(Reader reader) throws IOException {
        final kdlLexer lexer = new kdlLexer(CharStreams.fromReader(reader));
        final kdlParser parser = new kdlParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new BailErrorStrategy());

        final KDLVisitorImpl visitor = new KDLVisitorImpl();
        return (KDLDocument) visitor.visit(parser.parse());
    }
}
