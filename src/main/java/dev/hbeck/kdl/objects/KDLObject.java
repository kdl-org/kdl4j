package dev.hbeck.kdl.objects;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public interface KDLObject {
    void writeKDL(Writer writer) throws IOException;

    default String toKDL() {
        final StringWriter writer = new StringWriter();
        final BufferedWriter bufferedWriter = new BufferedWriter(writer);

        try {
            this.writeKDL(bufferedWriter);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return writer.toString();
    }
}
