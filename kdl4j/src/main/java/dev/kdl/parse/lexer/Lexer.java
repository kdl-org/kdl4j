package dev.kdl.parse.lexer;

import dev.kdl.parse.KdlParseException;
import dev.kdl.parse.context.ParseContext;
import dev.kdl.parse.context.Position;
import dev.kdl.parse.context.Span;
import dev.kdl.parse.lexer.token.Token;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;

/**
 * A KDL lexer.
 */
public interface Lexer extends AutoCloseable {

	/**
	 * Reads the next token and advances the lexer.
	 *
	 * @return the next token, or {@code null} if the end of the file has been reached
	 * @throws IOException       when an error occurs reading the input
	 * @throws KdlParseException when the parsed document is not valid
	 */
	@Nullable
	Token read() throws IOException, KdlParseException;

	/**
	 * Peeks the next token, without advancing the lexer.
	 *
	 * @return the next token, or {@code null} if the end of the file has been reached
	 * @throws IOException       when an error occurs reading the input
	 * @throws KdlParseException when the parsed document is not valid
	 */
	@Nullable
	default Token peek() throws IOException, KdlParseException {
		return peek(0);
	}

	/**
	 * Peeks a token, without advancing the lexer. It is 0-based, therefore {@code peek(0)} is the same as {@code peek()}.
	 *
	 * @param n the index of the token to peek
	 * @return the nth token, or {@code null} if the end of the file has been reached
	 * @throws IOException       when an error occurs reading the input
	 * @throws KdlParseException when the parsed document is not valid
	 */
	@Nullable
	Token peek(int n) throws IOException, KdlParseException;

	/**
	 * Creates a parse context to attach to an error. The current line is entirely read, to correctly display it.
	 *
	 * @param startLine the first line to include in the context
	 * @param endLine   the last line to include in the context
	 * @param span      the span to highlight
	 * @return a parse context
	 * @throws IOException when an error occurs reading the input
	 */
	@Nonnull
	ParseContext getErrorParseContext(int startLine, int endLine, @Nonnull Span span) throws IOException;

	/**
	 * Creates a parse context to attach to an error. The current line is entirely read, to correctly display it.
	 *
	 * @param span the span to highlight
	 * @return a parse context
	 * @throws IOException when an error occurs reading the input
	 */
	@Nonnull
	default ParseContext getErrorParseContext(@Nonnull Span span) throws IOException {
		return getErrorParseContext(span.start().line(), span.end().line(), span);
	}

	/**
	 * Creates a parse context for the next token to attach to an error. The current line is entirely read, to correctly
	 * display it.
	 *
	 * @return a parse context
	 * @throws IOException when an error occurs reading the input
	 */
	@Nonnull
	ParseContext getErrorParseContextForNextPosition() throws IOException;

	/**
	 * @return the starting position of the next token
	 */
	@Nonnull
	Position getNextPosition();

	@Override
	void close() throws IOException;
}
