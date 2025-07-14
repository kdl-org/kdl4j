package dev.kdl.parse.lexer;

import dev.kdl.parse.KdlInternalParseException;
import dev.kdl.parse.KdlParseException;
import dev.kdl.parse.context.ParseContext;
import dev.kdl.parse.context.Position;
import dev.kdl.parse.context.SourceLines;
import dev.kdl.parse.context.Span;
import dev.kdl.parse.lexer.reader.KdlReader;
import dev.kdl.parse.lexer.token.Token;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.util.function.Function;

import static dev.kdl.parse.lexer.helper.Kdl2CharHelper.isNewline;
import static dev.kdl.parse.lexer.reader.KdlReader.EOF;

abstract class AbstractKdlLexer implements Lexer {

	AbstractKdlLexer(@Nullable String filename, @Nonnull KdlReader reader, int capacity) {
		this.filename = filename;
		this.reader = reader;
		this.readTokens = new RingBuffer<>(capacity);
	}

	/**
	 * Reads the stream for the next token.
	 *
	 * @return the next token, or {@code null} if the end of file has been reached.
	 * @throws IOException       when an error occurs while reading
	 * @throws KdlParseException when there is a syntax error
	 */
	@Nullable
	protected abstract Token nextToken() throws IOException, KdlParseException;

	@Nullable
	@Override
	public Token read() throws IOException, KdlParseException {
		if (!readTokens.isEmpty()) {
			return readTokens.removeFirst();
		}
		return nextToken();
	}

	@Nullable
	@Override
	public Token peek(int n) throws IOException, KdlParseException {
		if (n < 0 || n >= readTokens.capacity()) {
			throw new KdlInternalParseException("Error while peeking: n should be between 0 and " + (readTokens.capacity() - 1) + " included but was " + n);
		}

		while (readTokens.size() <= n) {
			var token = nextToken();
			if (token == null) {
				return null;
			}
			readTokens.addLast(token);
		}
		return readTokens.get(n);
	}

	@Nonnull
	@Override
	public ParseContext getErrorParseContext(int startLine, int endLine, @Nonnull Span span) throws IOException {
		while (true) {
			var c = readChar();
			if (c == EOF || isNewline(c)) {
				sourceLines.newline();
				break;
			}
		}
		return new ParseContext(filename, sourceLines.getLines(startLine, endLine), span);
	}

	@Nonnull
	@Override
	public ParseContext getErrorParseContextForNextPosition() throws IOException {
		if (readTokens.isEmpty()) {
			return getErrorParseContext(Span.of(sourceLines.getNextPosition()));
		}
		return getErrorParseContext(readTokens.get(0).span());
	}

	@Nonnull
	@Override
	public Position getNextPosition() {
		if (readTokens.isEmpty()) {
			return sourceLines.getNextPosition();
		}
		return readTokens.get(0).span().start();
	}

	/**
	 * Reads the next character.
	 *
	 * @return the next character, or {@link KdlReader#EOF} if the end of file has been reached
	 * @throws IOException when there is an error reading the stream
	 */
	protected int readChar() throws IOException {
		var c = reader.read();
		if (c != EOF) {
			sourceLines.append(c);
		}
		return c;
	}

	/**
	 * Consumes the next character.
	 *
	 * @throws IOException when there is an error reading the stream
	 */
	protected void consumeChar() throws IOException {
		consumeChar(1);
	}

	/**
	 * Consumes the next characters.
	 *
	 * @param n the number of characters to consume
	 * @throws IOException when there is an error reading the stream
	 */
	protected void consumeChar(int n) throws IOException {
		for (var i = 0; i < n; i++) {
			readChar();
		}
	}

	/**
	 * Peeks the next character.
	 *
	 * @return the next character, or {@link KdlReader#EOF} if the end of file has been reached
	 * @throws IOException when there is an error reading the stream
	 */
	protected int peekChar() throws IOException {
		return reader.peek();
	}

	/**
	 * Peeks a character ahead.
	 *
	 * @param n the index of the character to peek, 0-based.
	 * @return the peeked character, or {@link KdlReader#EOF} if the end of file has been reached
	 * @throws IOException when there is an error reading the stream
	 */
	protected int peekChar(int n) throws IOException {
		return reader.peek(n);
	}

	/**
	 * Consumes the next character and creates a new token with the provided function.
	 *
	 * @param createToken a function that creates a token from its span
	 * @param <T>         the type of the created token
	 * @return a new token created with {@code createToken}
	 * @throws IOException when there is an error reading the stream
	 */
	@Nonnull
	protected <T extends Token> T consumeAndCreate(Function<Span, T> createToken) throws IOException {
		consumeChar();
		return createToken.apply(Span.of(sourceLines.getCurrentPosition()));
	}

	@Nonnull
	private final KdlReader reader;
	@Nonnull
	private final RingBuffer<Token> readTokens;
	@Nullable
	private final String filename;
	/**
	 * The source lines for the parsed document.
	 */
	@Nonnull
	protected final SourceLines sourceLines = new SourceLines();
}
