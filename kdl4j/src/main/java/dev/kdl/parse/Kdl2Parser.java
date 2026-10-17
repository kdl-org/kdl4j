package dev.kdl.parse;

import dev.kdl.KdlDocument;
import dev.kdl.KdlNode;
import dev.kdl.parse.context.Span;
import dev.kdl.parse.lexer.Kdl2Lexer;
import dev.kdl.parse.lexer.token.Boolean;
import dev.kdl.parse.lexer.token.Brace.ClosingBrace;
import dev.kdl.parse.lexer.token.Brace.OpeningBrace;
import dev.kdl.parse.lexer.token.ByteOrderMark;
import dev.kdl.parse.lexer.token.EqualsSign;
import dev.kdl.parse.lexer.token.Newline;
import dev.kdl.parse.lexer.token.NodeSpace;
import dev.kdl.parse.lexer.token.Null;
import dev.kdl.parse.lexer.token.Number;
import dev.kdl.parse.lexer.token.Parentheses.ClosingParentheses;
import dev.kdl.parse.lexer.token.Parentheses.OpeningParentheses;
import dev.kdl.parse.lexer.token.Semicolon;
import dev.kdl.parse.lexer.token.SingleLineComment;
import dev.kdl.parse.lexer.token.Slashdash;
import dev.kdl.parse.lexer.token.StringToken;
import dev.kdl.parse.lexer.token.Token;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parser for the KDL 2.0 syntax.
 */
public class Kdl2Parser implements KdlParser {

	@Nonnull
	@Override
	public KdlDocument parse(@Nullable String filename, @Nonnull InputStream inputStream) throws IOException, KdlParseException {
		return new Kdl2ParserContext(filename, inputStream).parse();
	}

	private static final class Kdl2ParserContext extends KdlParserContext {

		private Kdl2ParserContext(@Nullable String filename, @Nonnull InputStream inputStream) {
			super(new Kdl2Lexer(filename, inputStream, LEXER_CAPACITY));
		}

		@Override
		@Nonnull
		KdlDocument parse() throws IOException, KdlParseException {
			return document();
		}

		@Nonnull
		private KdlDocument document() throws IOException, KdlParseException {
			consumeByteOrderMark();
			return new KdlDocument(nodes(true));
		}

		private void consumeByteOrderMark() throws IOException, KdlParseException {
			if (lexer.peek() instanceof ByteOrderMark) {
				lexer.read();
			}
		}

		@Override
		protected boolean isIgnorable(@Nonnull Token token) {
			return token instanceof NodeSpace
				|| token instanceof Newline
				|| token instanceof Semicolon
				|| token instanceof SingleLineComment;
		}

		@Nonnull
		private List<KdlNode> nodes(boolean isRoot) throws IOException, KdlParseException {
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

				if (!nodeTerminator() && (isRoot || !(peek() instanceof ClosingBrace))) {
					throw new KdlParseException(
						"Semi-colon expected between nodes on the same line",
						lexer.getErrorParseContext(Span.of(lexer.getNextPosition())),
						"semi-colon expected",
						"nodes need to be separated by a semi-colon or a newline character"
					);
				}
			}

			lineSpaces();

			return nodes;
		}

		@Nullable
		private KdlNode node() throws KdlParseException, IOException {
			var nodeStart = lexer.getNextPosition();
			var type = type();
			consumeToken(NodeSpace.class);

			var nameToken = string();

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
			var children = nodeChildren();
			consumeToken(NodeSpace.class);

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

		@Nullable
		private Type type() throws IOException, KdlParseException {
			var openingParentheses = parseToken(OpeningParentheses.class);
			if (openingParentheses == null) {
				return null;
			}

			var nodeSpace = parseToken(NodeSpace.class);
			var type = expectToken(
				StringToken.class,
				"Missing type name in type annotation",
				"string expected",
				() -> nodeSpace == null ? null : nodeSpace.span()
			);
			consumeToken(NodeSpace.class);
			var closingParentheses = expectToken(
				ClosingParentheses.class,
				"Missing closing parentheses in type annotation",
				"closing parentheses expected",
				() -> new Span(openingParentheses.span().start(), type.span().end())
			);

			return new Type(type.value(), new Span(openingParentheses.span().start(), closingParentheses.span().end()));
		}

		@Nonnull
		private Pair<List<ParsedValue>, List<ParsedProperty>> argumentsAndProperties() throws IOException, KdlParseException {
			var arguments = new ArrayList<ParsedValue>();
			var properties = new ArrayList<ParsedProperty>();

			while (isArgumentOrProperty()) {
				consumeToken(NodeSpace.class);
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
			}

			return new Pair<>(arguments, properties);
		}

		private boolean isArgumentOrProperty() throws IOException, KdlParseException {
			if (peek(0) instanceof NodeSpace) {
				if (peek(1) instanceof Slashdash) {
					return isValueStart(peek(2));
				} else {
					return isValueStart(peek(1));
				}
			}
			return peek(0) instanceof Slashdash && isValueStart(peek(1));
		}

		private boolean isValueStart(Token token) {
			return token instanceof OpeningParentheses
				|| token instanceof Number
				|| token instanceof Boolean
				|| token instanceof Null
				|| token instanceof StringToken;
		}

		@Nullable
		private StringToken getPropertyName() throws IOException, KdlParseException {
			var firstToken = peek(0);
			if (firstToken instanceof StringToken stringToken) {
				if (peek(1) instanceof NodeSpace && peek(2) instanceof EqualsSign) {
					consume(3);
					return stringToken;
				} else if (peek(1) instanceof EqualsSign) {
					consume(2);
					return stringToken;
				}
			}
			return null;
		}

		@Nonnull
		private ParsedValue expectValue(String errorMessage) throws IOException, KdlParseException {
			var type = type();
			consumeToken(NodeSpace.class);

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
		private List<KdlNode> nodeChildren() throws IOException, KdlParseException {
			List<KdlNode> children = null;

			var openingBrace = getOpeningBrace();
			while (openingBrace != null) {
				if (openingBrace.first() != null) {
					nodes(false);
					expectClosingBrace(openingBrace.second().span().start());
				} else if (children != null) {
					throw new KdlParseException(
						"More than one list of children provided for node",
						lexer.getErrorParseContext(openingBrace.second().span()),
						"second children list"
					);
				} else {
					children = nodes(false);
					expectClosingBrace(openingBrace.second().span().start());
				}
				openingBrace = getOpeningBrace();
			}

			return children == null ? Collections.emptyList() : children;
		}

		@Nullable
		private Pair<Slashdash, OpeningBrace> getOpeningBrace() throws IOException, KdlParseException {
			var offset = peek(0) instanceof NodeSpace ? 1 : 0;
			var token = peek(offset);
			if (token instanceof Slashdash slashdash) {
				var nextToken = peek(offset + 1);
				if (nextToken instanceof OpeningBrace openingBrace) {
					consume(offset + 2);
					return new Pair<>(slashdash, openingBrace);
				}
			} else if (token instanceof OpeningBrace openingBrace) {
				consume(offset + 1);
				return new Pair<>(null, openingBrace);
			}
			return null;
		}

		private boolean nodeTerminator() throws IOException, KdlParseException {
			return peek() == null || consumeToken(SingleLineComment.class, Newline.class, Semicolon.class);
		}

		@Nullable
		private StringToken string() throws IOException, KdlParseException {
			return parseToken(StringToken.class);
		}

		private void lineSpaces() throws IOException, KdlParseException {
			while (true) {
				if (!consumeToken(NodeSpace.class, Newline.class, SingleLineComment.class)) {
					break;
				}
			}
		}

		@Override
		protected Token read() throws IOException, KdlParseException {
			return checkBom(super.read());
		}

		@Override
		protected Token peek(int n) throws IOException, KdlParseException {
			return checkBom(lexer.peek(n));
		}

		private Token checkBom(Token token) throws KdlParseException, IOException {
			if (token instanceof ByteOrderMark) {
				throw new KdlParseException(
					"Unexpected byte-order mark after start",
					lexer.getErrorParseContext(token.span()),
					"byte-order mark",
					"byte-order mark can only appear as the first character of the document"
				);
			}

			return token;
		}

		private static final int LEXER_CAPACITY = 3;

	}

}
