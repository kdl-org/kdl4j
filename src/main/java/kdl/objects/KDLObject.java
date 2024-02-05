package kdl.objects;

import kdl.print.PrintConfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * The base interface all objects parsed from a KDL document must implement
 */
public interface KDLObject {

    /**
     * Write the object to the provided stream.
     *
     * @param writer the Writer to write to
     * @param printConfig a configuration object controlling how the object is printed
     * @throws IOException if there is any issue writing the object
     */
    void writeKDL(Writer writer, PrintConfig printConfig) throws IOException;

    /**
     * Generate a string with the text representation of the given object.
     *
     * @return the string
     */
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
