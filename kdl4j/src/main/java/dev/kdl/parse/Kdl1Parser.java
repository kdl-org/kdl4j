package dev.kdl.parse;

import dev.kdl.KdlDocument;
import dev.kdl.KdlNode;
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

/**
 * Parser for the KDL 1.0 syntax.
 */
public class Kdl1Parser implements KdlParser {
	@Nonnull
	@Override
	public KdlDocument parse(@Nullable String filename, @Nonnull InputStream inputStream) throws IOException, KdlParseException {
		return new Kdl1ParserContext(filename, inputStream).parse();
	}

	private static final class Kdl1ParserContext extends KdlParserContext {

		private Kdl1ParserContext(@Nullable String filename, @Nonnull InputStream inputStream) {
			super(new Kdl1Lexer(filename, inputStream, LEXER_CAPACITY));
		}

		@Nonnull
		@Override
		KdlDocument parse() throws IOException, KdlParseException {
			return new KdlDocument(nodes());
		}

		@Override
		protected boolean isIgnorable(@Nonnull Token token) {
			return token instanceof Whitespace
				|| token instanceof LineContinuation
				|| token instanceof Newline
				|| token instanceof Semicolon
				|| token instanceof SingleLineComment;
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
			var nodeStart = lexer.getNextPosition();
			var type = type();

			var nameToken = identifier();

			if (nameToken == null) {
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

			var lastSignificantToken = getLastSignificantToken();
			var nodeSpan = new Span(nodeStart, lastSignificantToken == null ? nodeStart : lastSignificantToken.span().end());

			return buildNode(
				type == null ? null : type.value(),
				type == null ? null : type.span(),
				nameToken.value(),
				nameToken.span(),
				argumentsAndProperties.first(),
				argumentsAndProperties.second(),
				children,
				nodeSpan
			);
		}

		@Nonnull
		private Pair<List<ParsedValue>, List<ParsedProperty>> argumentsAndProperties() throws IOException, KdlParseException {
			var arguments = new ArrayList<ParsedValue>();
			var properties = new ArrayList<ParsedProperty>();

			while (true) {
				if (nodeSpaces() && isArgumentOrProperty()) {
					var isSlashdash = consumeToken(Slashdash.class);
					var propertyName = getPropertyName();
					if (propertyName != null) {
						var value = expectValue("Missing property value");
						if (!isSlashdash) {
							properties.add(new ParsedProperty(propertyName.value(), propertyName.span(), value));
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

			return new Pair<>(arguments, properties);
		}

		private boolean isArgumentOrProperty() throws IOException, KdlParseException {
			if (peek(0) instanceof Slashdash) {
				return isValueStart(peek(1));
			}
			return isValueStart(peek(0));
		}

		private boolean isValueStart(Token token) {
			return token instanceof Parentheses.OpeningParentheses
				|| token instanceof Number
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
		private ParsedValue expectValue(String errorMessage) throws IOException, KdlParseException {
			var type = type();

			var token = read();
			if (token instanceof Number || token instanceof Boolean || token instanceof Null || token instanceof StringToken) {
				var typeValue = type == null ? null : type.value();
				var typeSpan = type == null ? null : type.span();
				var span = typeSpan == null ? token.span() : new Span(typeSpan.start(), token.span().end());
				return new ParsedValue(typeValue, typeSpan, token, span);
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
		private Type type() throws IOException, KdlParseException {
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

			var closingParentheses = expectToken(
				Parentheses.ClosingParentheses.class,
				"Missing closing parentheses in type annotation",
				"closing parentheses expected",
				() -> new Span(openingParentheses.span().start(), type.span().end())
			);

			return new Type(type.value(), new Span(openingParentheses.span().start(), closingParentheses.span().end()));
		}

		@Nullable
		private Token identifier() throws IOException, KdlParseException {
			var token = peek();
			if (token instanceof StringToken || token instanceof BareIdentifier) {
				consume(1);
				return token;
			}
			return null;
		}

		private void nodeTerminator() throws IOException, KdlParseException {
			var token = read();
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
