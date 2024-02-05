package kdl.print;

import java.io.IOException;
import java.io.Writer;

import static kdl.parse.CharClasses.getEscapeIncludingUnicode;
import static kdl.parse.CharClasses.isValidBareId;

public class PrintUtil {
    public static void writeStringQuotedAppropriately(Writer writer, String string, boolean bareAllowed, PrintConfig printConfig) throws IOException {
        if (string.isEmpty()) {
            writer.write("\"\"");
            return;
        }

        if (bareAllowed && isValidBareId(string)) {
            writer.write(string);
            return;
        }

        writer.write('"');
        for (int i = 0; i < string.length(); i++) {
            final int c = string.charAt(i);
            if (printConfig.requiresEscape(c)) {
                writer.write(getEscapeIncludingUnicode(c));
            } else {
                writer.write(c);
            }
        }
        writer.write('"');
    }
}
