package dev.hbeck.kdl.print;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static dev.hbeck.kdl.parse.CharClasses.isCommonEscape;
import static dev.hbeck.kdl.parse.CharClasses.isNonAscii;
import static dev.hbeck.kdl.parse.CharClasses.isPrintableAscii;
import static dev.hbeck.kdl.parse.CharClasses.isUnicodeLinespace;
import static dev.hbeck.kdl.parse.CharClasses.isUnicodeWhitespace;
import static dev.hbeck.kdl.parse.CharClasses.mustEscape;

/**
 * A config object controlling various aspects of how KDL documents are printed.
 */
public class PrintConfig {
    public static final PrintConfig PRETTY_DEFAULT = PrintConfig.builder().build();
    public static final PrintConfig RAW_DEFAULT = PrintConfig.builder()
            .setIndent(0)
            .setEscapeNonAscii(false)
            .setPrintEmptyChildren(false)
            .build();

    private final Map<Integer, Boolean> escapes;
    private final boolean escapeNonPrintableAscii;
    private final boolean escapeLinespace;
    private final boolean escapeNonAscii;
    private final boolean escapeCommon;
    private final boolean requireSemicolons;
    private final boolean respectRadix;
    private final String newline;
    private final int indent;
    private final char indentChar;
    private final char exponentChar;
    private final boolean printEmptyChildren;
    private final boolean printNullArgs;
    private final boolean printNullProps;

    private PrintConfig(Map<Integer, Boolean> escapes, boolean escapeNonPrintableAscii, boolean escapeLinespace,
                        boolean escapeNonAscii, boolean escapeCommon, boolean requireSemicolons, boolean respectRadix, String newline,
                        int indent, char indentChar, char exponentChar, boolean printEmptyChildren, boolean printNullArgs,
                        boolean printNullProps) {

        this.escapes = Collections.unmodifiableMap(escapes);
        this.escapeNonPrintableAscii = escapeNonPrintableAscii;
        this.escapeLinespace = escapeLinespace;
        this.escapeNonAscii = escapeNonAscii;
        this.escapeCommon = escapeCommon;
        this.requireSemicolons = requireSemicolons;
        this.respectRadix = respectRadix;
        this.newline = newline;
        this.indent = indent;
        this.indentChar = indentChar;
        this.exponentChar = exponentChar;
        this.printEmptyChildren = printEmptyChildren;
        this.printNullArgs = printNullArgs;
        this.printNullProps = printNullProps;
    }

    public boolean requiresEscape(int c) {
        if (shouldForceEscape(c)) {
            return true;
        } else if (mustEscape(c)) {
            return true;
        } else if (escapeLinespace && isUnicodeLinespace(c)) {
            return true;
        } else if (escapeNonPrintableAscii && !isNonAscii(c) && !isPrintableAscii(c)) {
            return true;
        } else if (escapeNonAscii && isNonAscii(c)) {
            return true;
        } else if (escapeCommon && isCommonEscape(c)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if character has been set to force strings containing it to be escaped
     *
     * @param c the character to check
     * @return true if the character should be escaped, false otherwise.
     */
    public boolean shouldForceEscape(int c) {
        return escapes.getOrDefault(c, false);
    }

    public boolean shouldEscapeNonPrintableAscii() {
        return escapeNonPrintableAscii;
    }

    public boolean shouldEscapeStandard() {
        return escapeCommon;
    }

    /**
     * @return how many getIndentChar() characters lines will be indented for each level they are away from the root.
     *         If 0, no indentation will be performed
     */
    public int getIndent() {
        return indent;
    }

    /**
     * @return the character used to indent lines
     */
    public char getIndentChar() {
        return indentChar;
    }

    /**
     * @return the character used to indicate the beginning of the exponent part of floating point numbers
     */
    public char getExponentChar() {
        return exponentChar;
    }

    /**
     * @return true if empty children should be printed with braces containing no nodes, false if they shouldn't be printed
     */
    public boolean shouldPrintEmptyChildren() {
        return printEmptyChildren;
    }

    /**
     * @return true if node arguments with the literal value 'null' will be printed
     */
    public boolean shouldPrintNullArgs() {
        return printNullArgs;
    }

    /**
     * @return true if node properties with the literal value 'null' will be printed
     */
    public boolean shouldPrintNullProps() {
        return printNullProps;
    }

    /**
     * @return true if each node should be terminated with a ';', false if semicolons will be omitted entirely
     */
    public boolean shouldRequireSemicolons() {
        return requireSemicolons;
    }

    /**
     * @return true if each number should be printed with its specified radix, false if they should be printed just base-10
     */
    public boolean shouldRespectRadix() {
        return respectRadix;
    }

    /**
     * @return get the string used to print newlines
     */
    public String getNewline() {
        return newline;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * See get()/should() methods above for explanation of each variable's meaning
     */
    public static class Builder {
        private final Map<Integer, Boolean> escapes = new HashMap<>();

        private boolean requireSemicolons = false;
        private boolean escapeNonAscii = false;
        private boolean escapeNonPrintableAscii = true;
        private boolean escapeCommon = true;
        private boolean escapeLinespace = true;
        private boolean respectRadix = true;
        private String newline = "\n";
        private int indent = 4;
        private char indentChar = ' ';
        private char exponentChar = 'E';
        private boolean printEmptyChildren = true;
        private boolean printNullArgs = true;
        private boolean printNullProps = true;

        public Builder setForceEscapeChar(int c) {
            escapes.put(c, true);
            return this;
        }

        public Builder setEscapeNonPrintableAscii(boolean escapeNonPrintableAscii) {
            this.escapeNonPrintableAscii = escapeNonPrintableAscii;
            return this;
        }

        public Builder setEscapeNonAscii(boolean escapeNonAscii) {
            this.escapeNonAscii = escapeNonAscii;
            return this;
        }

        public Builder setEscapeCommon(boolean escapeCommon) {
            this.escapeCommon = escapeCommon;
            return this;
        }

        public Builder setEscapeLinespace(boolean escapeLinespace) {
            this.escapeLinespace = escapeLinespace;
            return this;
        }

        public Builder setRespectRadix(boolean respectRadix) {
            this.respectRadix = respectRadix;
            return this;
        }

        public Builder setIndent(int indent) {
            this.indent = indent;
            return this;
        }

        public Builder setIndentChar(char indentChar) {
            this.indentChar = indentChar;
            return this;
        }

        public Builder setExponentChar(char exponentChar) {
            this.exponentChar = exponentChar;
            return this;
        }

        public Builder setPrintEmptyChildren(boolean printEmptyChildren) {
            this.printEmptyChildren = printEmptyChildren;
            return this;
        }

        public Builder setPrintNullArgs(boolean printNullArgs) {
            this.printNullArgs = printNullArgs;
            return this;
        }

        public Builder setPrintNullProps(boolean printNullProps) {
            this.printNullProps = printNullProps;
            return this;
        }

        public Builder setRequireSemicolons(boolean requireSemicolons) {
            this.requireSemicolons = requireSemicolons;
            return this;
        }

        public Builder setNewline(String newline) {
            this.newline = newline;
            return this;
        }

        public PrintConfig build() {
            if (exponentChar != 'e' && exponentChar != 'E') {
                throw new IllegalArgumentException("Exponent character must be either 'e' or 'E'");
            }

            for (int i = 0; i < newline.length(); i++) {
                if (!isUnicodeLinespace(newline.charAt(i))) {
                    throw new IllegalArgumentException("All characters in specified 'newline' must be unicode vertical space");
                }
            }

            if (!isUnicodeWhitespace(indentChar)) {
                throw new IllegalArgumentException("Indent character must be unicode whitespace");
            }

            return new PrintConfig(escapes, escapeNonPrintableAscii, escapeLinespace, escapeNonAscii, escapeCommon,
                    requireSemicolons, respectRadix, newline, indent, indentChar, exponentChar,
                    printEmptyChildren, printNullArgs, printNullProps);
        }
    }
}
