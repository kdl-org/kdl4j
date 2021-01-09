package dev.hbeck.kdl.parse;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.hbeck.kdl.parse.KDLParserV2.EOF;

public class KDLParseContext {
    private static final Set<Integer> UNICODE_LINESPACE = Stream.of('\r', '\n', '\u0085', '\u000C', '\u2028', '\u2029')
            .map(character -> (int) character)
            .collect(Collectors.toSet());

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
        } else if (UNICODE_LINESPACE.contains(c)) {
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
        if (UNICODE_LINESPACE.contains(c)) {
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

    public String getCurrentPosition() {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Line ").append(lineNumber).append(":\n")
                .append(lines.peek()).append('\n');

        for (int i = 0; i < positionInLine; i++) {
            stringBuilder.append('-');
        }

        return stringBuilder.append('^').toString();
    }
}
