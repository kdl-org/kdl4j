package kdl.parse;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import kdl.KDLBoolean;
import kdl.KDLDocument;
import kdl.KDLNode;
import kdl.KDLNull;
import kdl.KDLString;
import kdl.KDLValue;
import kdl.Properties;
import kdl.parse.lexer.Lexer;
import kdl.parse.lexer.Token;
import kdl.parse.lexer.token.Bom;
import kdl.parse.lexer.token.Boolean;
import kdl.parse.lexer.token.EqualsSign;
import kdl.parse.lexer.token.Escline;
import kdl.parse.lexer.token.MultiLineComment;
import kdl.parse.lexer.token.Newline;
import kdl.parse.lexer.token.Null;
import kdl.parse.lexer.token.Number;
import kdl.parse.lexer.token.Semicolon;
import kdl.parse.lexer.token.SingleLineComment;
import kdl.parse.lexer.token.Slashdash;
import kdl.parse.lexer.token.StringToken;
import kdl.parse.lexer.token.Whitespace;

import static kdl.parse.lexer.token.Brace.CLOSING_BRACE;
import static kdl.parse.lexer.token.Brace.OPENING_BRACE;
import static kdl.parse.lexer.token.Parentheses.CLOSING_PARENTHESES;
import static kdl.parse.lexer.token.Parentheses.OPENING_PARENTHESES;

/**
 * Entry point for parsing a KDL document.
 */
public class KDLParser {

	/**
	 * Parses the provided string as a {@link  KDLDocument}.
	 *
	 * @param document a string representation of a valid KDL document
	 * @return a {@link KDLDocument} corresponding to {@code document}
	 * @throws IOException if an error occurs while reading the input
	 */
	@Nonnull
	public static KDLDocument parse(@Nonnull String document) throws IOException {
		return new KDLParser(new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8))).parse();
	}

	/**
	 * Parses a file as a {@link  KDLDocument}.
	 *
	 * @param path path to a file containing a valid KDL document
	 * @return a {@link KDLDocument} corresponding to {@code document}
	 * @throws IOException if an error occurs while reading the input
	 */
	@Nonnull
	public static KDLDocument parse(@Nonnull Path path) throws IOException {
		try (var fileInputStream = Files.newInputStream(path)) {
			return new KDLParser(new BufferedInputStream(fileInputStream)).parse();
		}
	}

	/**
	 * Parses the provided input stream as a {@link  KDLDocument}. The input stream
	 *
	 * @param inputStream an input stream returning a valid KDL document
	 * @return a {@link KDLDocument} corresponding to {@code document}
	 * @throws IOException if an error occurs while reading the input
	 */
	@Nonnull
	public static KDLDocument parse(@Nonnull InputStream inputStream) throws IOException {
		return new KDLParser(inputStream).parse();
	}

	private KDLParser(InputStream inputStream) {
		lexer = new Lexer(inputStream);
	}

	@Nonnull
	private KDLDocument parse() throws IOException {
		return document();
	}

	@Nonnull
	private KDLDocument document() throws IOException {
		consumeByteOrderMark();
		return new KDLDocument(nodes(true));
	}

	private void consumeByteOrderMark() throws IOException {
		parseToken(Bom.INSTANCE::equals);
	}

	@Nonnull
	private List<KDLNode> nodes(boolean nodeTerminatorRequired) throws IOException {
		var nodes = new ArrayList<KDLNode>();

		while (true) {
			repeat(this::lineSpace);
			var node = node();
			if (node == null) {
				break;
			}

			nodes.add(node.node);

			if (!node.hasNodeTerminator) {
				if (nodeTerminatorRequired) {
					throw new KDLParseException(lexer.error("node terminator expected"));
				}
				break;
			}
		}

		return nodes;
	}

	private boolean plainLineSpace() throws IOException {
		return parseToken(is(Newline.class), this::isWhiteSpace, is(SingleLineComment.class));
	}

	private boolean plainNodeSpace() throws IOException {
		var hasAtLeastOneWhiteSpace = repeat(this::whitespace);

		var token = lexer.peek();
		if (token instanceof Escline) {
			lexer.next();
			repeat(this::whitespace);
			return true;
		}

		return hasAtLeastOneWhiteSpace;
	}

	private boolean lineSpace() throws IOException {
		if (lexer.peek() instanceof Slashdash) {
			lexer.next();
			repeat(this::plainNodeSpace);
			var node = node();
			if (node == null) {
				throw new KDLParseException(lexer.error("a node is required after a slashdash ('/-')"));
			} else if (!node.hasNodeTerminator) {
				throw new KDLParseException(lexer.error("node terminator expected"));
			}
			return true;
		}
		return repeat(this::plainLineSpace);
	}

	private boolean nodeSpace() throws IOException {
		if (!repeat(this::plainNodeSpace)) {
			return false;
		}

		if (lexer.peek() instanceof Slashdash) {
			lexer.next();
			repeat(this::plainNodeSpace);
			var token = lexer.peek();
			if (token == OPENING_BRACE) {
				nodeChildren();
			} else {
				nodePropOrArg();
			}
		}

		return true;
	}

	private void requiredNodeSpace() throws IOException {
		repeat(this::nodeSpace);
		plainNodeSpace();
	}

	private void optionalNodeSpace() throws IOException {
		repeat(this::nodeSpace);
	}

	@Nullable
	private KDLNode baseNode() throws IOException {
		var type = type();
		optionalNodeSpace();

		var name = string();
		if (name == null) {
			if (type == null) {
				return null;
			} else {
				throw new KDLParseException(lexer.error("node name expected"));
			}
		}

		var propOrArgs = new LinkedList<PropertyOrArgument>();
		List<KDLNode> children = null;

		while (true) {
			var token = lexer.peek();
			if (!(isWhiteSpace(token) || token instanceof Escline)) {
				break;
			}
			requiredNodeSpace();

			token = lexer.peek();
			if (token instanceof StringToken) {
				lexer.next();
				if (lexer.peek() instanceof EqualsSign) {
					var value = propertyValue(token.value());
					propOrArgs.addLast(new PropertyOrArgument.Property(token.value(), value));
				} else {
					propOrArgs.addLast(new PropertyOrArgument.Argument(new KDLString(Optional.empty(), token.value())));
				}
			} else if (token instanceof EqualsSign) {
				lexer.next();
				var previous = propOrArgs.pollLast();
				if (!(previous instanceof PropertyOrArgument.Argument)) {
					throw new KDLParseException(lexer.error("unexpected equals sign (" + token.value() + ')'));
				}
				propOrArgs.addLast(((PropertyOrArgument.Argument) previous).asProperty(value()));
			} else if (token == OPENING_BRACE) {
				children = nodeChildren();
				break;
			} else {
				var value = value();
				if (value != null) {
					propOrArgs.addLast(new PropertyOrArgument.Argument(value));
				}
			}
		}

		var arguments = new ArrayList<KDLValue<?>>();
		var properties = Properties.builder();
		for (var propOrArg : propOrArgs) {
			if (propOrArg instanceof PropertyOrArgument.Argument) {
				arguments.add(((PropertyOrArgument.Argument) propOrArg).value);
			} else {
				var property = (PropertyOrArgument.Property) propOrArg;
				properties.property(property.name, property.value);
			}
		}

		return new KDLNode(
			Optional.ofNullable(type),
			name.value(),
			arguments,
			properties.build(),
			children == null ? Collections.emptyList() : children
		);
	}

	@Nullable
	private Node node() throws IOException {
		var node = baseNode();
		if (node == null) {
			return null;
		}

		optionalNodeSpace();

		return new Node(node, nodeTerminator());
	}

	private void nodePropOrArg() throws IOException {
		var propOrArg = lexer.peek();
		if (propOrArg instanceof StringToken) {
			lexer.next();
			optionalNodeSpace();
			if (lexer.peek() instanceof EqualsSign) {
				lexer.next();
				optionalNodeSpace();
				var value = value();
				if (value == null) {
					throw new KDLParseException(lexer.error("value expected for property " + propOrArg.value()));
				}
			}
			return;
		}
		var value = value();
		if (value == null) {
			throw new KDLParseException(lexer.error("property or argument expected"));
		}
	}

	@Nonnull
	private KDLValue<?> propertyValue(String property) throws IOException {
		lexer.next();
		var value = value();
		if (value == null) {
			throw new KDLParseException(lexer.error("value expected for property " + property));
		}
		return value;
	}

	@Nonnull
	private List<KDLNode> nodeChildren() throws IOException {
		lexer.next();
		var nodes = nodes(false);

		if (!parseToken(CLOSING_BRACE::equals)) {
			throw new KDLParseException(lexer.error("closing brace expected"));
		}

		return nodes;
	}

	private boolean nodeTerminator() throws IOException {
		return parseToken(is(SingleLineComment.class), is(Newline.class), Semicolon.INSTANCE::equals, Objects::isNull);
	}

	@Nullable
	private KDLValue<?> value() throws IOException {
		var type = Optional.ofNullable(type());
		optionalNodeSpace();

		var token = lexer.peek();
		if (token instanceof Number) {
			lexer.next();
			return ((Number) token).asKDLNumber(type);
		} else if (token instanceof Boolean) {
			lexer.next();
			return new KDLBoolean(type, token == Boolean.TRUE);
		} else if (token instanceof Null) {
			lexer.next();
			return new KDLNull(type);
		} else if (token instanceof StringToken) {
			lexer.next();
			return new KDLString(type, token.value());
		}

		if (type.isPresent()) {
			throw new KDLParseException(lexer.error("value expected after type"));
		}

		return null;
	}

	@Nullable
	private String type() throws IOException {
		if (lexer.peek() != OPENING_PARENTHESES) {
			return null;
		}
		lexer.next();

		optionalNodeSpace();
		var type = string();
		if (type == null) {
			throw new KDLParseException(lexer.error("type expected"));
		}
		optionalNodeSpace();

		if (lexer.peek() != CLOSING_PARENTHESES) {
			throw new KDLParseException(lexer.error("closing parentheses expected"));
		}
		lexer.next();

		return type.value();
	}

	@Nullable
	private StringToken string() throws IOException {
		var token = lexer.peek();
		return token instanceof StringToken ? (StringToken) lexer.next() : null;
	}

	private boolean whitespace() throws IOException {
		var token = lexer.peek();
		if (isWhiteSpace(token)) {
			lexer.next();
			return true;
		}
		return false;
	}

	private boolean isWhiteSpace(Token token) {
		return token instanceof Whitespace || token instanceof MultiLineComment;
	}

	@SafeVarargs
	private boolean parseToken(Predicate<Token>... predicates) throws IOException {
		var token = lexer.peek();

		for (var predicate : predicates) {
			if (predicate.test(token)) {
				lexer.next();
				return true;
			}
		}

		return false;
	}

	private final Lexer lexer;

	private static boolean repeat(ParseFunction parseFunction) throws IOException {
		var result = false;
		while (true) {
			if (!parseFunction.parse()) {
				return result;
			}
			result = true;
		}
	}

	@Nonnull
	private static <T extends Token> Predicate<Token> is(@Nonnull Class<T> tokenClass) {
		return token -> token != null && tokenClass.isAssignableFrom(token.getClass());
	}

	@FunctionalInterface
	private interface ParseFunction {

		boolean parse() throws IOException;
	}

	private interface PropertyOrArgument {
		final class Property implements PropertyOrArgument {
			public Property(@Nonnull String name, @Nonnull KDLValue<?> value) {
				this.name = name;
				this.value = value;
			}

			@Nonnull
			private final String name;
			@Nonnull
			private final KDLValue<?> value;
		}

		final class Argument implements PropertyOrArgument {
			public Argument(@Nonnull KDLValue<?> value) {
				this.value = value;
			}

			@Nonnull
			Property asProperty(KDLValue<?> value) {
				return new Property((String) this.value.getValue(), value);
			}

			@Nonnull
			private final KDLValue<?> value;
		}
	}

	private static final class Node {
		public Node(@Nonnull KDLNode node, boolean hasNodeTerminator) {
			this.node = node;
			this.hasNodeTerminator = hasNodeTerminator;
		}

		@Nonnull
		private final KDLNode node;
		private final boolean hasNodeTerminator;
	}
}
