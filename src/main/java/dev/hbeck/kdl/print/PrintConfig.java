package dev.hbeck.kdl.print;

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

    public boolean shouldEscapeNewlines() {
        return escapeNewlines;
    }

    public boolean shouldEscapeCommon() {
        return escapeCommon;
    }

    public boolean shouldEscapeNonAscii() {
        return escapeNonAscii;
    }

    public int getIndent() {
        return indent;
    }

    public char getIndentChar() {
        return indentChar;
    }

    public char getExponentChar() {
        return exponentChar;
    }

    public boolean shouldPrintEmptyChildren() {
        return printEmptyChildren;
    }

    public boolean shouldPrintNullArgs() {
        return printNullArgs;
    }

    public boolean shouldPrintNullProps() {
        return printNullProps;
    }

    public boolean shouldRequireSemicolons() {
        return requireSemicolons;
    }

    public String getNewline() {
        return newline;
    }

    public static Builder builder() {
        return new Builder();
    }
    
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
            return new PrintConfig(requireSemicolons, newline, escapeCommon, escapeNonAscii, indent, indentChar, exponentChar,
                    printEmptyChildren, printNullArgs, printNullProps, escapeNewlines);
        }
    }
}
