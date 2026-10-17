package dev.kdl.parse;

import dev.kdl.KdlBoolean;
import dev.kdl.KdlDocument;
import dev.kdl.KdlNode;
import dev.kdl.KdlNull;
import dev.kdl.KdlProperties;
import dev.kdl.KdlProperty;
import dev.kdl.KdlString;
import dev.kdl.KdlValue;
import dev.kdl.parse.context.ParseContext;
import dev.kdl.parse.context.Position;
import dev.kdl.parse.context.Span;
import dev.kdl.parse.lexer.Lexer;
import dev.kdl.parse.lexer.token.Boolean;
import dev.kdl.parse.lexer.token.Brace;
import dev.kdl.parse.lexer.token.Null;
import dev.kdl.parse.lexer.token.Number;
import dev.kdl.parse.lexer.token.StringToken;
import dev.kdl.parse.lexer.token.Token;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

abstract class KdlParserContext {
	KdlParserContext(Lexer lexer) {
		this.lexer = lexer;
	}

	@Nonnull
	abstract KdlDocument parse() throws IOException, KdlParseException;

	/**
	 * Checks if a token does not contribute to the content of a node (whitespace, newline, comment, separator).
	 *
	 * @param token the token to check
	 * @return {@code true} if the token can be ignored when computing the span of a node
	 */
	protected abstract boolean isIgnorable(@Nonnull Token token);

	protected Token read() throws IOException, KdlParseException {
		var token = lexer.read();
		if (token != null && !isIgnorable(token)) {
			lastSignificantToken = token;
		}
		return token;
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

	/**
	 * Creates a parse context for a sub-element (value, property, ...) of a node, reusing the source lines of the node
	 * context.
	 *
	 * @param nodeContext the context of the node containing the element
	 * @param span        the span of the element
	 * @return a context for the element
	 */
	@Nonnull
	protected ParseContext contextFor(@Nonnull ParseContext nodeContext, @Nonnull Span span) {
		return new ParseContext(nodeContext.filename(), nodeContext.sourceLines(), span);
	}

	/**
	 * Builds a {@link KdlValue} from a parsed value and a parse context.
	 *
	 * @param parsed  the parsed value, containing its type and token
	 * @param context the context in which the value was parsed
	 * @return the built value
	 */
	@Nonnull
	protected KdlValue<?> createValue(@Nonnull ParsedValue parsed, @Nullable ParseContext context) {
		var token = parsed.token();
		if (token instanceof Number number) {
			return number.asKDLNumber(parsed.type(), parsed.typeSpan(), context);
		} else if (token instanceof Boolean aBoolean) {
			return new KdlBoolean(parsed.type(), aBoolean.booleanValue(), parsed.typeSpan(), context);
		} else if (token instanceof Null) {
			return new KdlNull(parsed.type(), parsed.typeSpan(), context);
		} else if (token instanceof StringToken stringToken) {
			return new KdlString(parsed.type(), stringToken.value(), parsed.typeSpan(), context);
		}
		throw new KdlInternalParseException("Unexpected token type in parsed value: " + token.getClass().getName());
	}

	/**
	 * Builds a node from its parsed parts, attaching the parse contexts of the node, its arguments and its properties.
	 *
	 * @param type             the type of the node
	 * @param typeSpan         the span of the node's type annotation, if any
	 * @param name             the name of the node
	 * @param nameSpan         the span of the node's name
	 * @param parsedArguments  the parsed arguments of the node
	 * @param parsedProperties the parsed properties of the node
	 * @param children         the children of the node
	 * @param nodeSpan         the span of the whole node
	 * @return the built node
	 */
	@Nonnull
	protected KdlNode buildNode(
		@Nullable String type,
		@Nullable Span typeSpan,
		@Nonnull String name,
		@Nullable Span nameSpan,
		@Nonnull List<ParsedValue> parsedArguments,
		@Nonnull List<ParsedProperty> parsedProperties,
		@Nonnull List<KdlNode> children,
		@Nonnull Span nodeSpan
	) {
		var nodeContext = lexer.getParseContext(nodeSpan);

		var arguments = new ArrayList<KdlValue<?>>(parsedArguments.size());
		for (var parsed : parsedArguments) {
			arguments.add(createValue(parsed, contextFor(nodeContext, parsed.span())));
		}

		var properties = KdlProperties.builder();
		for (var parsed : parsedProperties) {
			var propertyContext = contextFor(nodeContext, new Span(parsed.nameSpan().start(), parsed.value().span().end()));
			properties.property(new KdlProperty<>(
				parsed.name(),
				createValue(parsed.value(), contextFor(nodeContext, parsed.value().span())),
				parsed.nameSpan(),
				propertyContext
			));
		}

		return new KdlNode(type, name, arguments, properties.build(), children, nodeContext, nameSpan, typeSpan);
	}

	/**
	 * @return the last token that is part of the content of a node, or {@code null} if none was read yet
	 */
	@Nullable
	protected final Token getLastSignificantToken() {
		return lastSignificantToken;
	}

	protected final Lexer lexer;

	@Nullable
	private Token lastSignificantToken;

	protected record Pair<T, U>(T first, U second) {
	}

	/**
	 * A parsed type annotation.
	 *
	 * @param value the value of the type
	 * @param span  the span of the whole type annotation, including the parentheses
	 */
	protected record Type(@Nonnull String value, @Nonnull Span span) {
	}

	/**
	 * A parsed value, before its parse context is known.
	 *
	 * @param type     the type of the value
	 * @param typeSpan the span of the type annotation, if any
	 * @param token    the token of the value
	 * @param span     the span of the whole value, including the type annotation if any
	 */
	protected record ParsedValue(
		@Nullable String type,
		@Nullable Span typeSpan,
		@Nonnull Token token,
		@Nonnull Span span
	) {
	}

	/**
	 * A parsed property, before the parse contexts are known.
	 *
	 * @param name     the name of the property
	 * @param nameSpan the span of the property's name
	 * @param value    the value of the property
	 */
	protected record ParsedProperty(@Nonnull String name, @Nonnull Span nameSpan, @Nonnull ParsedValue value) {
	}
}
