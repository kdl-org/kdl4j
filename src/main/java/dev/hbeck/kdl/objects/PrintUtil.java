package dev.hbeck.kdl.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

class PrintUtil {
    private static final Predicate<String> VALID_BARE_ID = Pattern.compile(
            "^[^\n\r\u000C\u0085\u2028\u2029{}<>;\\\\\\[\\]=,\"\u0009\u0020\u00A0\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u202F\u205F\u30000-9]" +
            "[^\n\r\u000C\u0085\u2028\u2029{}<>;\\\\\\[\\]=,\"\u0009\u0020\u00A0\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u202F\u205F\u3000]*$"
    ).asPredicate();

    private static final Predicate<String> NO_ESCAPE_REQUIRED = str -> !str.contains("\\") && !str.contains("\"");

    static void writeStringQuotedAppropriately(Writer writer, String string, boolean bareAllowed) throws IOException {
        int quoteAt = string.indexOf('"');
        int hashDepth = 0;
        while (quoteAt >= 0) {
            int hashesNeeded = 1;
            for (int i = quoteAt + 1; i < string.length() && string.charAt(i) == '#'; i++) {
                hashesNeeded++;
            }
            hashDepth = Math.max(hashDepth, hashesNeeded);
            quoteAt = string.indexOf('"', quoteAt + 1);
        }

        if (hashDepth == 0 && NO_ESCAPE_REQUIRED.test(string)) {
            if (bareAllowed && VALID_BARE_ID.test(string)) {
                writer.write(string);
            } else {
                writer.write('"');
                writer.write(string);
                writer.write('"');
            }
        } else {
            writer.write('r');
            for (int i = 0; i < hashDepth; i++) {
                writer.write('#');
            }
            writer.write('"');
            writer.write(string);
            writer.write('"');
            for (int i = 0; i < hashDepth; i++) {
                writer.write('#');
            }
        }
    }
}
