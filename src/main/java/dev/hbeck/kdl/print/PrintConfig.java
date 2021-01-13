package dev.hbeck.kdl.print;

import static dev.hbeck.kdl.parse.CharClasses.isUnicodeLinespace;
import static dev.hbeck.kdl.parse.CharClasses.isUnicodeWhitespace;

/**
 * A config object controlling various aspects of how KDL documents are printed.
 */
public class PrintConfig {
    public static final PrintConfig PRETTY_DEFAULT = PrintConfig.builder().build();
    public static final PrintConfig RAW_DEFAULT = PrintConfig.builder()
            .setIndent(0)
            .setPrintEmptyChildren(false)
            .setEscapeNewlines(true)
            .build();

    private final boolean requireSemicolons;
    private final String newline;
    private final int indent;
    private final char indentChar;
    private final char exponentChar;
    private final boolean escapeCommon;
    private final boolean escapeNonAscii;
    private final boolean printEmptyChildren;
    private final boolean printNullArgs;
    private final boolean printNullProps;
    private final boolean escapeNewlines;

    private PrintConfig(boolean requireSemicolons, String newline, boolean escapeCommon, boolean escapeNonAscii, int indent, char indentChar,
                        char exponentChar, boolean printEmptyChildren, boolean printNullArgs, boolean printNullProps, boolean escapeNewlines) {
        this.requireSemicolons = requireSemicolons;
        this.newline = newline;
        this.escapeCommon = escapeCommon;
        this.escapeNonAscii = escapeNonAscii;
        this.indent = indent;
        this.indentChar = indentChar;
        this.exponentChar = exponentChar;
        this.printEmptyChildren = printEmptyChildren;
        this.printNullArgs = printNullArgs;
        this.printNullProps = printNullProps;
        this.escapeNewlines = escapeNewlines;
    }

    /**
     * @return true if newlines in escaped strings will be represented with an escape sequence, false if they'll be
     *         represented with a literal newline.
     */
    public boolean shouldEscapeNewlines() {
        return escapeNewlines;
    }

    /**
     *
     *
     * @return true if common ascii escape characters other than newlines will be escaped, false if they'll be
     *         represented by their literal values
     */
    public boolean shouldEscapeCommon() {
        return escapeCommon;
    }

    /**
     * @return true if any character outside the printable ascii range should be escaped, false if they'll be
     *         represented by their literal values
     */
    public boolean shouldEscapeNonPrintableAscii() {
        return escapeNonAscii;
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
        private boolean requireSemicolons = false;
        private String newline = "\n";
        private boolean escapeCommon = true;
        private boolean escapeNonAscii = false;
        private boolean escapeNewlines = false;
        private int indent = 4;
        private char indentChar = ' ';
        private char exponentChar = 'E';
        private boolean printEmptyChildren = true;
        private boolean printNullArgs = true;
        private boolean printNullProps = true;

        public Builder setEscapeCommon(boolean escapeCommon) {
            this.escapeCommon = escapeCommon;
            return this;
        }

        public Builder setEscapeNonAscii(boolean escapeNonAscii) {
            this.escapeNonAscii = escapeNonAscii;
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

        public Builder setEscapeNewlines(boolean escapeNewlines) {
            this.escapeNewlines = escapeNewlines;
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

            return new PrintConfig(requireSemicolons, newline, escapeCommon, escapeNonAscii, indent, indentChar, exponentChar,
                    printEmptyChildren, printNullArgs, printNullProps, escapeNewlines);
        }
    }
}
