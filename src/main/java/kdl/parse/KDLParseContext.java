package kdl.parse;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;

import static kdl.parse.KDLParser.EOF;

/**
 * Internal class wrapping the stream containing the document being read. Maintains a list of the last three lines read
 * in order to provide context in the event of a parse error.
 */
public class KDLParseContext {
    private final PushbackReader reader;
    private final Deque<StringBuilder> lines;

    private int positionInLine;
    private int lineNumber;

    private boolean invalidated;

    public KDLParseContext(Reader reader) {
        this.lines = new ArrayDeque<>();
        lines.push(new StringBuilder());

        this.reader = new PushbackReader(reader, 2);
        this.positionInLine = 0;
        this.lineNumber = 1;
        this.invalidated = false;
    }

    /**
     * Read a character from the underlying stream. Stores it in a buffer as well for error reporting.
     *
     * @return the character read or EOF if the stream has been exhausted
     * @throws IOException if any error is encountered in the stream read operation
     */
    public int read() throws IOException {
        if (invalidated) {
            throw new KDLInternalException("Attempt to read from an invalidated context");
        }

        int c = reader.read();
        if (c == EOF) {
            return c;
        } else if (CharClasses.isUnicodeLinespace(c)) {
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

    /**
     * Pushes a single character back into the stream. If this method and the peek() function are invoked more than
     * two times without a read() in between an exception will be thrown.
     *
     * @param c the character to be pushed
     */
    public void unread(int c) {
        if (invalidated) {
            throw new KDLInternalException("Attempt to unread from an invalidated context");
        }

        if (CharClasses.isUnicodeLinespace(c)) {
            lines.pop();
            lineNumber--;
            positionInLine = lines.peek().length() - 1;
        } else if (c == EOF) {
            throw new KDLInternalException("Attempted to unread() EOF");
        } else {
            positionInLine--;
            final StringBuilder currLine = lines.peek();
            currLine.deleteCharAt(currLine.length() - 1);
        }

        try {
            reader.unread(c);
        } catch (IOException e) {
            throw new KDLInternalException("Attempted to unread more than 2 characters in sequence", e);
        }
    }

    /**
     * Gets the next character in the stream without consuming it. See unread() for a note on calling this function
     *
     * @return the next character in the stream
     * @throws IOException if any error occurs reading from the stream
     */
    public int peek() throws IOException {
        if (invalidated) {
            throw new KDLInternalException("Attempt to peek at an invalidated context");
        }

        int c = reader.read();
        if (c != -1) {
            reader.unread(c);
        }

        return c;
    }

    /**
     * For use following parse and internal errors for error reporting. Invalidates the context, after which any
     * following operation on the context will fail. Reads the remainder of the current line and returns a string
     * holding the current line followed by a pointer to the character where the context had read to prior to this call.
     *
     * @return the string outlined above
     */
    public String getErrorLocationAndInvalidateContext() {
        if (invalidated) {
            throw new KDLInternalException("Attempted to getErrorLocationAndInvalidateContext from an invalid context");
        }
        invalidated = true;

        final StringBuilder stringBuilder = new StringBuilder();
        final StringBuilder line = lines.peek();
        if (line == null) {
            throw new KDLInternalException("Attempted to report an error, but there were no line objects in the stack");
        }

        try {
            int c = reader.read();
            while (!CharClasses.isUnicodeLinespace(c) && c != EOF) {
                line.appendCodePoint(c);
                c = reader.read();
            }
        } catch (IOException e) {
            line.append("<Read Error>");
        }

        stringBuilder.append("Line ").append(lineNumber).append(":\n")
                .append(line).append('\n');

        for (int i = 0; i < positionInLine - 1; i++) {
            stringBuilder.append('-');
        }

        return stringBuilder.append('^').toString();
    }
}
