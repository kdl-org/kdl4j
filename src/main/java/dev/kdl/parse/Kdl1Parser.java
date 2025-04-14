package dev.kdl.parse;

import dev.kdl.KdlBoolean;
import dev.kdl.KdlDocument;
import dev.kdl.KdlNode;
import dev.kdl.KdlNull;
import dev.kdl.KdlProperties;
import dev.kdl.KdlString;
import dev.kdl.KdlValue;
import dev.kdl.parse.context.Span;
import dev.kdl.parse.lexer.Kdl1Lexer;
import dev.kdl.parse.lexer.token.BareIdentifier;
import dev.kdl.parse.lexer.token.Boolean;
import dev.kdl.parse.lexer.token.Brace;
import dev.kdl.parse.lexer.token.EqualsSign;
import dev.kdl.parse.lexer.token.LineContinuation;
import dev.kdl.parse.lexer.token.Newline;
import dev.kdl.parse.lexer.token.Null;
import dev.kdl.parse.lexer.token.Number;
import dev.kdl.parse.lexer.token.Parentheses;
import dev.kdl.parse.lexer.token.Semicolon;
import dev.kdl.parse.lexer.token.SingleLineComment;
import dev.kdl.parse.lexer.token.Slashdash;
import dev.kdl.parse.lexer.token.StringToken;
import dev.kdl.parse.lexer.token.Token;
import dev.kdl.parse.lexer.token.Whitespace;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Kdl1Parser implements KdlParser {
	@Nonnull
	@Override
	public KdlDocument parse(@Nonnull String filename, @Nonnull InputStream inputStream) throws IOException, KdlParseException {
		try (var kdl1ParserContext = new Kdl1ParserContext(filename, inputStream)) {
			return kdl1ParserContext.parse();
		}
	}

	private static final class Kdl1ParserContext extends KdlParserContext {

		private Kdl1ParserContext(@Nonnull String filename, @Nonnull InputStream inputStream) {
			super(new Kdl1Lexer(filename, inputStream, LEXER_CAPACITY));
		}

		@Nonnull
		@Override
		KdlDocument parse() throws IOException, KdlParseException {
			return new KdlDocument(nodes());
		}

		@Nonnull
		private List<KdlNode> nodes() throws IOException, KdlParseException {
			var nodes = new ArrayList<KdlNode>();

			while (true) {
				lineSpaces();
				var slashdash = parseToken(Slashdash.class);
				var node = node();

				if (node == null) {
					if (slashdash != null) {
						var errorSpan = Span.of(lexer.getNextPosition());
						throw new KdlParseException(
							"Valid node expected after slashdash",
							lexer.getErrorParseContext(slashdash.span().start().line(), errorSpan.end().line(), errorSpan),
							"node expected"
						);
					}
					break;
				}

				if (slashdash == null) {
					nodes.add(node);
				}
			}

			lineSpaces();

			var token = lexer.peek();
			if (token instanceof LineContinuation) {
				throw new KdlParseException(
					"unexpected line continuation between nodes",
					lexer.getErrorParseContext(token.span()),
					"line continuation",
					"line continuations can only be used inside a node"
				);
			}

			return nodes;
		}

		@Nullable
		private KdlNode node() throws IOException, KdlParseException {
			var type = type();

			var name = identifier();

			if (name == null) {
				if (type == null) {
					return null;
				} else {
					throw new KdlParseException(
						"Missing node name after node type",
						lexer.getErrorParseContextForNextPosition(),
						"string expected"
					);
				}
			}

			var argumentsAndProperties = argumentsAndProperties();
			var children = children();
			nodeSpaces();
			nodeTerminator();

			return new KdlNode(
				type,
				name,
				argumentsAndProperties.first(),
				argumentsAndProperties.second(),
				children
			);
		}

		@Nonnull
		private Pair<List<KdlValue<?>>, KdlProperties> argumentsAndProperties() throws IOException, KdlParseException {
			var arguments = new ArrayList<KdlValue<?>>();
			var properties = KdlProperties.builder();

			while (true) {
				if (nodeSpaces() && isArgumentOrProperty()) {
					var isSlashdash = consumeToken(Slashdash.class);
					var propertyName = getPropertyName();
					if (propertyName != null) {
						var value = expectValue("Missing property value");
						if (!isSlashdash) {
							properties.property(propertyName.value(), value);
						}
					} else {
						var value = expectValue("Missing value after argument type");
						if (!isSlashdash) {
							arguments.add(value);
						}
					}
				} else {
					break;
				}
			}

			return new Pair<>(arguments, properties.build());
		}

		private boolean isArgumentOrProperty() throws IOException, KdlParseException {
			if (peek(0) instanceof Slashdash) {
				return isValueStart(peek(1));
			}
			return isValueStart(peek(0));
		}

		private boolean isValueStart(Token token) {
			return token instanceof Parentheses.OpeningParentheses
				|| token instanceof dev.kdl.parse.lexer.token.Number
				|| token instanceof Boolean
				|| token instanceof Null
				|| token instanceof StringToken
				|| token instanceof BareIdentifier;
		}

		@Nullable
		private Token getPropertyName() throws IOException, KdlParseException {
			var name = peek(0);
			if ((name instanceof StringToken || name instanceof BareIdentifier) && peek(1) instanceof EqualsSign) {
				consume(2);
				return name;
			}
			return null;
		}

		@Nonnull
		private KdlValue<?> expectValue(String errorMessage) throws IOException, KdlParseException {
			var type = type();

			var token = read();
			if (token instanceof Number number) {
				return number.asKDLNumber(type);
			} else if (token instanceof Boolean aBoolean) {
				return new KdlBoolean(type, aBoolean.booleanValue());
			} else if (token instanceof Null) {
				return new KdlNull(type);
			} else if (token instanceof StringToken) {
				return new KdlString(type, token.value());
			}

			var errorSpan = token == null
				? Span.of(lexer.getNextPosition())
				: token.span();
			throw new KdlParseException(
				errorMessage,
				lexer.getErrorParseContext(errorSpan),
				"value expected"
			);
		}

		@Nonnull
		private List<KdlNode> children() throws IOException, KdlParseException {
			nodeSpaces();

			var openingBrace = getOpeningBrace();
			if (openingBrace != null) {
				var nodes = nodes();
				expectClosingBrace(openingBrace.second().span().start());
				if (openingBrace.first() == null) {
					return nodes;
				}
			}

			return Collections.emptyList();
		}

		@Nullable
		private Pair<Slashdash, Brace.OpeningBrace> getOpeningBrace() throws IOException, KdlParseException {
			var token = peek(0);
			if (token instanceof Slashdash slashdash) {
				var secondToken = peek(1);
				if (secondToken instanceof Brace.OpeningBrace openingBrace) {
					consume(2);
					return new Pair<>(slashdash, openingBrace);
				}
			} else if (token instanceof Brace.OpeningBrace openingBrace) {
				consume(1);
				return new Pair<>(null, openingBrace);
			}
			return null;
		}

		@Nullable
		private String type() throws IOException, KdlParseException {
			var openingParentheses = parseToken(Parentheses.OpeningParentheses.class);
			if (openingParentheses == null) {
				return null;
			}

			var type = read();
			if (!(type instanceof BareIdentifier || type instanceof StringToken)) {
				throw new KdlParseException(
					"Missing type name in type annotation",
					lexer.getErrorParseContext(type.span()),
					"string or identifier expected"
				);
			}

			expectToken(
				Parentheses.ClosingParentheses.class,
				"Missing closing parentheses in type annotation",
				"closing parentheses expected",
				() -> new Span(openingParentheses.span().start(), type.span().end())
			);

			return type.value();
		}

		@Nullable
		private String identifier() throws IOException, KdlParseException {
			var token = peek();
			if (token instanceof StringToken || token instanceof BareIdentifier) {
				consume(1);
				return token.value();
			}
			return null;
		}

		private void nodeTerminator() throws IOException, KdlParseException {
			var token = lexer.read();
			if (!(token == null || token instanceof SingleLineComment || token instanceof Newline || token instanceof Semicolon)) {
				throw new KdlParseException(
					"missing node terminator",
					lexer.getErrorParseContext(token.span()),
					"unexpected character",
					"a node must be terminated by a newline, a single-line comment, a semicolon or the end of file"
				);
			}
		}

		private void lineSpaces() throws IOException, KdlParseException {
			while (true) {
				if (!consumeToken(Whitespace.class, Newline.class, SingleLineComment.class)) {
					break;
				}
			}
		}

		private boolean nodeSpaces() throws IOException, KdlParseException {
			var hasLength = false;

			while (consumeToken(Whitespace.class, LineContinuation.class)) {
				hasLength = true;
			}

			return hasLength;
		}

		private static final int LEXER_CAPACITY = 2;
	}
}
