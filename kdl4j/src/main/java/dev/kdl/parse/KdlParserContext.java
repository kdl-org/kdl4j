package dev.kdl.parse;

import dev.kdl.KdlDocument;
import dev.kdl.parse.context.Position;
import dev.kdl.parse.context.Span;
import dev.kdl.parse.lexer.Lexer;
import dev.kdl.parse.lexer.token.Brace;
import dev.kdl.parse.lexer.token.Token;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.util.function.Supplier;

abstract class KdlParserContext {
	KdlParserContext(Lexer lexer) {
		this.lexer = lexer;
	}

	@Nonnull
	abstract KdlDocument parse() throws IOException, KdlParseException;

	protected Token read() throws IOException, KdlParseException {
		return lexer.read();
	}

	protected Token peek() throws IOException, KdlParseException {
		return peek(0);
	}

	protected Token peek(int n) throws IOException, KdlParseException {
		return lexer.peek(n);
	}

	protected void consume(int n) throws IOException, KdlParseException {
		for (var i = 0; i < n; i++) {
			read();
		}
	}

	@SafeVarargs
	protected final boolean consumeToken(@Nonnull Class<? extends Token>... tokenClasses) throws IOException, KdlParseException {
		var token = peek();
		if (token != null) {
			for (var tokenClass : tokenClasses) {
				if (tokenClass.isAssignableFrom(token.getClass())) {
					read();
					return true;
				}
			}
		}
		return false;
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	protected <TOKEN extends Token> TOKEN expectToken(
		@Nonnull Class<TOKEN> tokenClass,
		@Nonnull String errorMessage,
		@Nonnull String errorLabel,
		@Nonnull Supplier<Span> spanSupplier
	) throws IOException, KdlParseException {
		var token = peek();
		if (token != null && tokenClass.isAssignableFrom(token.getClass())) {
			read();
			return (TOKEN) token;
		}
		var span = spanSupplier.get();
		if (span == null) {
			if (token == null) {
				span = Span.of(lexer.getNextPosition());
			} else {
				span = token.span();
			}
		}
		throw new KdlParseException(
			errorMessage,
			lexer.getErrorParseContext(span),
			errorLabel
		);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	protected <TOKEN extends Token> TOKEN parseToken(@Nonnull Class<TOKEN> tokenClass) throws IOException, KdlParseException {
		var token = peek();
		return token != null && tokenClass.isAssignableFrom(token.getClass()) ? (TOKEN) read() : null;
	}

	protected void expectClosingBrace(Position openingBracePosition) throws IOException, KdlParseException {
		var token = peek();
		if (token instanceof Brace.ClosingBrace) {
			read();
			return;
		}

		var span = token == null ? Span.of(lexer.getNextPosition()) : token.span();
		throw new KdlParseException(
			"Missing closing brace at the end of children list",
			lexer.getErrorParseContext(openingBracePosition.line(), span.end().line(), span),
			"closing brace expected"
		);
	}

	protected final Lexer lexer;

	protected record Pair<T, U>(T first, U second) {
	}
}
