package dev.hbeck.kdl.print;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static dev.hbeck.kdl.parse.CharClasses.getCommonEscape;
import static dev.hbeck.kdl.parse.CharClasses.getEscapeIncludingUnicode;
import static dev.hbeck.kdl.parse.CharClasses.isPrintableAscii;

public class PrintUtil {
    private static final Predicate<String> VALID_BARE_ID = Pattern.compile(
            "^[^\n\r\u000C\u0085\u2028\u2029{}<>;\\\\\\[\\]=,\"\u0009\u0020\u00A0\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u202F\u205F\u30000-9]" +
            "[^\n\r\u000C\u0085\u2028\u2029{}<>;\\\\\\[\\]=,\"\u0009\u0020\u00A0\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u202F\u205F\u3000]*$"
    ).asPredicate();

    public static void writeStringQuotedAppropriately(Writer writer, String string, boolean bareAllowed, PrintConfig printConfig) throws IOException {
        if (string.isEmpty()) {
            writer.write("\"\"");
            return;
        }

        int hashDepth = 0;
        if (!printConfig.shouldEscapeNonAscii()) {
            int quoteAt = string.indexOf('"');
            while (quoteAt >= 0) {
                int hashesNeeded = 1;
                for (int i = quoteAt + 1; i < string.length() && string.charAt(i) == '#'; i++) {
                    hashesNeeded++;
                }
                hashDepth = Math.max(hashDepth, hashesNeeded);
                quoteAt = string.indexOf('"', quoteAt + 1);
            }
        }

        if (hashDepth == 0 && !string.contains("\\") && !string.contains("\"")) {
            if (bareAllowed && VALID_BARE_ID.test(string)) {
                writer.write(string);
            } else {
                writer.write('"');
                for (int i = 0; i < string.length(); i++) {
                    final int c = string.charAt(i);
                    if (isPrintableAscii(c)) {
                        writer.write(c);
                    } else if (c == '\n' && printConfig.shouldEscapeNewlines()) {
                        writer.write("\\n");
                    } else if (printConfig.shouldEscapeNonAscii()) {
                        writer.write(getEscapeIncludingUnicode(c));
                    } else if (printConfig.shouldEscapeCommon()) {
                        final Optional<String> escape = getCommonEscape(c);
                        if (escape.isPresent()) {
                            writer.write(escape.get());
                        } else {
                            writer.write(c);
                        }
                    } else {
                        writer.write(c);
                    }
                }
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
