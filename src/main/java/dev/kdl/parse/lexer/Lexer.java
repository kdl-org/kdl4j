package dev.kdl.parse.lexer;

import dev.kdl.parse.KdlParseException;
import dev.kdl.parse.context.ParseContext;
import dev.kdl.parse.context.Position;
import dev.kdl.parse.context.Span;
import dev.kdl.parse.lexer.token.Token;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;

public interface Lexer extends AutoCloseable {
	@Nullable
	Token read() throws IOException, KdlParseException;

	@Nullable
	default Token peek() throws IOException, KdlParseException {
		return peek(0);
	}

	@Nullable
	Token peek(int n) throws IOException, KdlParseException;

	@Nonnull
	ParseContext getErrorParseContext(int startLine, int endLine, @Nonnull Span span) throws IOException;

	@Nonnull
	ParseContext getErrorParseContext(@Nonnull Span span) throws IOException;

	@Nonnull
	ParseContext getErrorParseContextForNextPosition() throws IOException;

	@Nonnull
	ParseContext getErrorParseContextForCurrentPosition() throws IOException;

	@Nonnull
	Position getNextPosition();

	@Override
	void close() throws IOException;
}
