package dev.hbeck.kdl.parse;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;

import static dev.hbeck.kdl.parse.CharClasses.isUnicodeLinespace;
import static dev.hbeck.kdl.parse.KDLParser.EOF;

public class KDLParseContext {
    private final PushbackReader reader;
    private final Deque<StringBuilder> lines;

    private int positionInLine;
    private int lineNumber;

    public KDLParseContext(Reader reader) {
        this.lines = new ArrayDeque<>();
        lines.push(new StringBuilder());

        this.reader = new PushbackReader(reader, 2);
        this.positionInLine = 0;
        this.lineNumber = 1;
    }

    public int read() throws IOException {
        int c = reader.read();
        if (c == EOF) {
            return c;
        } else if (isUnicodeLinespace(c)) {
            // We're cheating a bit here and not checking for CRLF
            positionInLine = 0;
            lineNumber++;
            lines.push(new StringBuilder());
            while (lines.size() > 3) {
                lines.removeLast();
            }
        } else {
            positionInLine++;
            lines.peek().appendCodePoint(c);
        }

        return c;
    }

    public void unread(int c) throws IOException {
        if (isUnicodeLinespace(c)) {
            lines.pop();
            lineNumber--;
            positionInLine = lines.peek().length() - 1;
        } else {
            positionInLine--;
            final StringBuilder currLine = lines.peek();
            currLine.deleteCharAt(currLine.length() - 1);
        }

        reader.unread(c);
    }

    public int peek() throws IOException {
        int c = reader.read();
        if (c != -1) {
            reader.unread(c);
        }

        return c;
    }

    public String getErrorLocationAndInvalidateContext() {
        final StringBuilder stringBuilder = new StringBuilder();
        final StringBuilder line = lines.peek();
        try {
            int c = reader.read();
            while (!isUnicodeLinespace(c) && c != EOF) {
                line.appendCodePoint(c);
                c = reader.read();
            }
        } catch (IOException e) {
            line.append("<Read Error>");
        }

        stringBuilder.append("Line ").append(lineNumber).append(":\n")
                .append(line).append('\n');

        for (int i = 0; i < positionInLine; i++) {
            stringBuilder.append('-');
        }

        return stringBuilder.append('^').toString();
    }
}
