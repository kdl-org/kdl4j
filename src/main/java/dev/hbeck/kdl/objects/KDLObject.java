package dev.hbeck.kdl.objects;

import dev.hbeck.kdl.print.PrintConfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public interface KDLObject {
    void writeKDL(Writer writer, PrintConfig printConfig) throws IOException;

    default String toKDL() {
        final StringWriter writer = new StringWriter();
        final BufferedWriter bufferedWriter = new BufferedWriter(writer);

        try {
            this.writeKDL(bufferedWriter, PrintConfig.PRETTY_DEFAULT);
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }
}
