package dev.kdl.parse;

import dev.kdl.KdlBoolean;
import dev.kdl.KdlDocument;
import dev.kdl.KdlNode;
import dev.kdl.KdlNull;
import dev.kdl.KdlProperties;
import dev.kdl.KdlString;
import dev.kdl.KdlValue;
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

public class Kdl2Parser implements KdlParser {

	@Nonnull
	@Override
	public KdlDocument parse(@Nullable String filename, @Nonnull InputStream inputStream) throws IOException, KdlParseException {
		try (var kdl2ParserContext = new Kdl2ParserContext(filename, inputStream)) {
			return kdl2ParserContext.parse();
		}
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
			var type = type();
			consumeToken(NodeSpace.class);

			var name = string();

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
			var children = nodeChildren();
			consumeToken(NodeSpace.class);

			return new KdlNode(
				type,
				name,
				argumentsAndProperties.first(),
				argumentsAndProperties.second(),
				children
			);
		}

		@Nullable
		private String type() throws IOException, KdlParseException {
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
			expectToken(
				ClosingParentheses.class,
				"Missing closing parentheses in type annotation",
				"closing parentheses expected",
				() -> new Span(openingParentheses.span().start(), type.span().end())
			);

			return type.value();
		}

		@Nonnull
		private Pair<List<KdlValue<?>>, KdlProperties> argumentsAndProperties() throws IOException, KdlParseException {
			var arguments = new ArrayList<KdlValue<?>>();
			var properties = KdlProperties.builder();

			while (isArgumentOrProperty()) {
				consumeToken(NodeSpace.class);
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
			}

			return new Pair<>(arguments, properties.build());
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
			if (firstToken instanceof StringToken) {
				if (peek(1) instanceof NodeSpace && peek(2) instanceof EqualsSign) {
					consume(3);
					return (StringToken) firstToken;
				} else if (peek(1) instanceof EqualsSign) {
					consume(2);
					return (StringToken) firstToken;
				}
			}
			return null;
		}

		@Nonnull
		private KdlValue<?> expectValue(String errorMessage) throws IOException, KdlParseException {
			var type = type();
			consumeToken(NodeSpace.class);

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
		private String string() throws IOException, KdlParseException {
			var token = parseToken(StringToken.class);
			return token == null ? null : token.value();
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
			return checkBom(lexer.read());
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
