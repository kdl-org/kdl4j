package dev.hbeck.kdl;

import dev.hbeck.kdl.parse.KDLParseContext;
import dev.hbeck.kdl.parse.KDLParser;

import java.io.IOException;
import java.io.StringReader;

public class TestUtil {
    public static final KDLParser parser = new KDLParser();

    public static KDLParseContext strToContext(String str) {
        final StringReader reader = new StringReader(str);
        return new KDLParseContext(reader);
    }

    public static String readRemainder(KDLParseContext context) {
        final StringBuilder stringBuilder = new StringBuilder();
        try {
            int read = context.read();
            while (read != KDLParser.EOF) {
                stringBuilder.appendCodePoint(read);
                read = context.read();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return stringBuilder.toString();
    }
}
