package dev.cbeck.kdl.objects;

import java.io.IOException;
import java.io.Writer;

class PrintUtil {
    static void writeStringQuotedAppropriately(Writer writer, String string, boolean bareAllowed) throws IOException {
        if (!bareAllowed) {
            writer.write('"');
        }
        writer.write(string);
        if (!bareAllowed) {
            writer.write('"');
        }
    }
}
