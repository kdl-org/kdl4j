package kdl.parse.lexer;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import kdl.parse.KDLParseException;
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

import static kdl.parse.error.ErrorUtils.errorMessage;
import static kdl.parse.lexer.token.Brace.CLOSING_BRACE;
import static kdl.parse.lexer.token.Brace.OPENING_BRACE;
import static kdl.parse.lexer.token.EqualsSign.isEqualsSign;
import static kdl.parse.lexer.token.Newline.CR;
import static kdl.parse.lexer.token.Newline.LF;
import static kdl.parse.lexer.token.Newline.isNewline;
import static kdl.parse.lexer.token.Number.isDigit;
import static kdl.parse.lexer.token.Number.isHexDigit;
import static kdl.parse.lexer.token.Number.isSign;
import static kdl.parse.lexer.token.Parentheses.CLOSING_PARENTHESES;
import static kdl.parse.lexer.token.Parentheses.OPENING_PARENTHESES;
import static kdl.parse.lexer.token.StringToken.IdentifierString.isDisallowedIdentifier;
import static kdl.parse.lexer.token.StringToken.IdentifierString.isIdentifierChar;
import static kdl.parse.lexer.token.StringToken.IdentifierString.isUnambiguousIdentifierChar;
import static kdl.parse.lexer.token.StringToken.IdentifierString.isUnicodeScalarValue;
import static kdl.parse.lexer.token.Whitespace.isWhitespace;

public class Lexer {
	public Lexer(InputStream inputStream) {
		this.reader = new KDLReader(inputStream);
	}

	@Nullable
	public Token next() throws IOException {
		if (token != null) {
			var result = token;
			token = null;
			return result;
		}
		return nextToken();
	}

	@Nullable
	public Token peek() throws IOException {
		if (token == null) {
			token = nextToken();
		}
		return token;
	}

	@Nullable
	private Token nextToken() throws IOException {
		var c = reader.read();

		switch (c) {
			case EOF:
				return null;
			case Bom.VALUE:
				return Bom.INSTANCE;
			case '/': {
				var second = reader.read();
				if (second == '/') { // <--
					return singleLineComment();
				} else if (second == '*') {
					return multiLineComment();
				} else if (second == '-') {
					return Slashdash.INSTANCE;
				}
				throw new KDLParseException(error("[/*-] expected after '/' but got '" + (char) second + "'"));
			}
			case '(':
				return OPENING_PARENTHESES;
			case ')':
				return CLOSING_PARENTHESES;
			case '{':
				return OPENING_BRACE;
			case '}':
				return CLOSING_BRACE;
			case ';':
				return Semicolon.INSTANCE;
			case '"':
				return quotedString();
			case '#': {
				var second = reader.peek();
				if (second == '#' || second == '"') {
					return rawString();
				}
				return keyword();
			}
			case '\\':
				return escline();
		}

		if (isNewline(c)) {
			if (c == CR && reader.peek() == LF) {
				reader.read();
				return new Newline("\r\n");
			}
			return new Newline((char) c);
		} else if (isWhitespace(c)) {
			return new Whitespace((char) c);
		} else if (isEqualsSign(c)) {
			return new EqualsSign((char) c);
		} else if (isSign(c) && isDigit(reader.peek()) || isDigit(c)) {
			return number(c);
		} else if (isIdentifierString(c)) {
			return identifierString(c);
		}

		throw new KDLParseException(error("invalid character '" + (char) c + '\''));
	}

	@Nonnull
	public String error(@Nonnull String message) {
		return reader.error(message);
	}

	@Nonnull
	private SingleLineComment singleLineComment() throws IOException {
		var value = new StringBuilder("//");

		while (true) {
			var c = reader.read();
			if (c != EOF) {
				value.appendCodePoint(c);
			}
			if (c == CR && reader.peek() == LF) {
				value.appendCodePoint(reader.read());
			}
			if (c == EOF || isNewline(c)) {
				return new SingleLineComment(value.toString());
			}
		}
	}

	@Nonnull
	private MultiLineComment multiLineComment() throws IOException {
		var value = new StringBuilder("/*");
		var expectedEnds = 1;

		while (true) {
			var c = reader.read();
			if (c == EOF) {
				throw new KDLParseException(error("unexpected end of file inside multi-line comment"));
			}
			value.appendCodePoint(c);
			if (c == '/' && reader.peek() == '*') {
				expectedEnds += 1;
			}
			if (c == '*' && reader.peek() == '/') {
				reader.read();
				value.append('/');
				expectedEnds -= 1;
				if (expectedEnds == 0) {
					return new MultiLineComment(value.toString());
				}
			}
		}
	}

	private boolean isIdentifierString(int c) throws IOException {
		if (isSign(c)) {
			var c2 = reader.peek();
			if (c2 == '.') {
				var c3 = reader.peek(2);
				return !isDigit(c3);
			}
			return !isDigit(c2);
		}
		if (c == '.') {
			var c2 = reader.peek();
			return !isDigit(c2);
		}
		return isUnambiguousIdentifierChar(c);
	}

	@Nonnull
	private StringToken.IdentifierString identifierString(int firstChar) throws IOException {
		var value = new StringBuilder();
		value.appendCodePoint(firstChar);

		while (isIdentifierChar(reader.peek())) {
			value.appendCodePoint(reader.read());
		}

		var stringValue = value.toString();
		if (isDisallowedIdentifier(stringValue)) {
			throw new KDLParseException(reader.error(
				"keyword used as identifier, use a quoted string instead",
				reader.line(),
				reader.column() - value.length()
			));
		}

		return new StringToken.IdentifierString(stringValue);
	}

	@Nonnull
	private StringToken.QuotedString quotedString() throws IOException {
		if (isNewline(reader.peek())) {
			reader.read();
			return multiLineQuotedString();
		}
		return singleLineQuotedString();
	}

	@Nonnull
	private StringToken.QuotedString multiLineQuotedString() throws IOException {
		var lines = new ArrayList<String>();
		var currentLine = new StringBuilder();

		while (true) {
			var c = reader.read();
			if (c == '"') {
				break;
			} else if (isNewline(c)) {
				lines.add(currentLine.toString());
				currentLine.setLength(0);
			} else {
				addStringCharacter(currentLine, c);
			}
		}

		var lastLine = currentLine.toString();
		return new StringToken.QuotedString(getMultiLineStringValue(lines, lastLine));
	}

	@Nonnull
	private StringToken.QuotedString singleLineQuotedString() throws IOException {
		var builder = new StringBuilder();
		while (true) {
			var c = reader.read();
			if (c == '"') {
				return new StringToken.QuotedString(builder.toString());
			} else if (isNewline(c)) {
				throw new KDLParseException(error("unexpected newline inside quoted string, escape it or use a multi-line quoted string"));
			} else {
				addStringCharacter(builder, c);
			}
		}
	}

	private void addStringCharacter(@Nonnull StringBuilder builder, int c) throws IOException {
		if (c == '\\') {
			var c2 = reader.read();
			var escapedCharacter = ESCAPED_CHARACTERS.get(c2);
			if (escapedCharacter != null) {
				builder.appendCodePoint(escapedCharacter);
			} else if (c2 == 'u') {
				builder.appendCodePoint(unicodeEscape());
			} else if (isWhitespace(c2) || isNewline(c2)) {
				whitespaceEscape();
			} else {
				throw new KDLParseException(error("invalid escape sequence '\\" + (char) c2 + '\''));
			}
		} else if (c == EOF) {
			throw new KDLParseException(error("unexpected end of file inside quoted string"));
		} else {
			builder.appendCodePoint(c);
		}
	}

	private int unicodeEscape() throws IOException {
		if (reader.read() != '{') {
			throw new KDLParseException(error("'{' expected at start of unicode escape"));
		}

		var count = 0;
		var hexValue = new StringBuilder();
		while (count < 6) {
			var c = reader.read();
			if (c == '}') {
				break;
			} else if (isHexDigit(c)) {
				count += 1;
				hexValue.appendCodePoint(c);
			} else {
				throw new KDLParseException(error("unexpected character in unicode escape"));
			}
		}

		if (count == 0) {
			throw new KDLParseException(error("at least one digit is required for a unicode escape"));
		} else if (count == 6) {
			var c = reader.read();
			if (c != '}') {
				throw new KDLParseException(error("'}' expected at end of unicode escape"));
			}
		}

		var codePoint = Integer.parseInt(hexValue.toString(), 16);
		if (!isUnicodeScalarValue(codePoint)) {
			throw new KDLParseException(reader.error(String.format("invalid unicode value U+%X", codePoint), reader.line(), reader.column() - count));
		}

		return codePoint;
	}

	private void whitespaceEscape() throws IOException {
		while (true) {
			var c = reader.peek();
			if (!isWhitespace(c) && !isNewline(c)) {
				return;
			}
			reader.read();
		}
	}

	@Nonnull
	private StringToken.QuotedString rawString() throws IOException {
		var openingSharpSigns = 1;
		while (reader.peek() == '#') {
			reader.read();
			openingSharpSigns += 1;
		}

		if (reader.read() != '"') {
			throw new KDLParseException(error("a quote is required to start a raw string"));
		}

		if (isNewline(reader.peek())) {
			reader.read();
			return multiLineRawString(openingSharpSigns);
		}

		return singleLineRawString(openingSharpSigns);
	}

	@Nonnull
	private StringToken.QuotedString multiLineRawString(int openingSharpSigns) throws IOException {
		var lines = new ArrayList<String>();
		var currentLine = new StringBuilder();

		var closingSharpSigns = 0;
		while (true) {
			var c = reader.read();
			if (c == EOF) {
				throw new KDLParseException(error("unexpected end of file inside raw string"));
			} else if (isNewline(c)) {
				writeNonClosingSharpSigns(currentLine, closingSharpSigns);
				closingSharpSigns = 0;
				lines.add(currentLine.toString());
				currentLine.setLength(0);
			} else if (c == '"' && reader.peek() == '#') {
				reader.read();
				if (openingSharpSigns == 1) {
					break;
				}
				closingSharpSigns = 1;
			} else if (c == '#' && closingSharpSigns > 0) {
				closingSharpSigns += 1;
				if (closingSharpSigns == openingSharpSigns) {
					break;
				}
			} else {
				writeNonClosingSharpSigns(currentLine, closingSharpSigns);
				closingSharpSigns = 0;
				currentLine.appendCodePoint(c);
			}
		}

		var lastLine = currentLine.toString();
		return new StringToken.QuotedString(getMultiLineStringValue(lines, lastLine));
	}

	@Nonnull
	private String getMultiLineStringValue(@Nonnull List<String> lines, @Nonnull String lastLine) {
		checkLastLine(lastLine);
		var builder = new StringBuilder();

		for (var i = 0; i < lines.size(); i++) {
			builder.append(removeIndent(lines.get(i), reader.line() - lines.size() + i, lastLine));
			if (i < lines.size() - 1) {
				builder.append('\n');
			}
		}

		return builder.toString();
	}

	private void checkLastLine(@Nonnull String lastLine) {
		var codePoints = lastLine.codePoints().boxed().collect(Collectors.toList());
		for (var column = 0; column < codePoints.size(); column++) {
			if (!isWhitespace(codePoints.get(column))) {
				throw new KDLParseException(reader.error("last line of a multi-line string must only contain whitespaces", reader.line(), column + 1));
			}
		}
	}

	@Nonnull
	private String removeIndent(@Nonnull String line, int lineNumber, @Nonnull String lastLine) {
		if (line.isEmpty()) {
			return line;
		}
		if (!line.startsWith(lastLine)) {
			throw new KDLParseException(errorMessage(
				line,
				"multi-line string indentation must match last line (\"" + lastLine + "\")",
				lineNumber,
				1
			));
		}
		return line.substring(lastLine.length());
	}

	@Nonnull
	private StringToken.QuotedString singleLineRawString(int openingSharpSigns) throws IOException {
		var builder = new StringBuilder();

		var closingSharpSigns = 0;
		while (true) {
			var c = reader.read();
			if (c == EOF) {
				throw new KDLParseException(error("unexpected end of file inside raw string"));
			} else if (isNewline(c)) {
				throw new KDLParseException(error("unexpected newline inside raw string, use a multi-line raw string"));
			} else if (c == '"' && reader.peek() == '#') {
				writeNonClosingSharpSigns(builder, closingSharpSigns);
				reader.read();
				if (openingSharpSigns == 1) {
					return new StringToken.QuotedString(builder.toString());
				}
				closingSharpSigns = 1;
			} else if (c == '#' && closingSharpSigns > 0) {
				closingSharpSigns += 1;
				if (closingSharpSigns == openingSharpSigns) {
					return new StringToken.QuotedString(builder.toString());
				}
			} else {
				writeNonClosingSharpSigns(builder, closingSharpSigns);
				closingSharpSigns = 0;
				builder.appendCodePoint(c);
			}
		}
	}

	private static void writeNonClosingSharpSigns(@Nonnull StringBuilder builder, int closingSharpSigns) {
		if (closingSharpSigns > 0) {
			builder.append('"');
			builder.append("#".repeat(closingSharpSigns));
		}
	}

	@Nonnull
	private Escline escline() throws IOException {
		var value = new StringBuilder("\\");

		while (true) {
			var c = reader.read();
			if (isWhitespace(c)) {
				value.appendCodePoint(c);
			} else if (c == '/') {
				var second = reader.read();
				if (second == '/') {
					var comment = singleLineComment();
					value.append(comment.value());
					break;
				} else if (second == '*') {
					var comment = multiLineComment();
					value.append(comment.value());
				} else {
					throw new KDLParseException(error("invalid character in escaped line"));
				}
			} else if (isNewline(c)) {
				value.appendCodePoint(c);
				break;
			} else if (c == EOF) {
				break;
			} else {
				throw new KDLParseException(error("invalid character in escaped line"));
			}
		}

		return new Escline(value.toString());
	}

	@Nonnull
	private Token keyword() throws IOException {
		var value = new StringBuilder();

		while (isIdentifierChar(reader.peek())) {
			value.appendCodePoint(reader.read());
		}

		switch (value.toString()) {
			case "true":
				return Boolean.TRUE;
			case "false":
				return Boolean.FALSE;
			case "null":
				return Null.INSTANCE;
			case "inf":
				return Number.INFINITY;
			case "-inf":
				return Number.MINUS_INFINITY;
			case "nan":
				return Number.NAN;
			default:
				throw new KDLParseException(reader.error("invalid value #" + value, reader.line(), reader.column() - value.length()));
		}
	}

	@Nonnull
	private Number number(int firstChar) throws IOException {
		var builder = new StringBuilder();
		if (firstChar == '-') {
			builder.append('-');
		}

		var firstDigit = isDigit(firstChar) ? firstChar : reader.read();
		if (firstDigit == '0') {
			var peek = reader.peek();
			if (peek == 'x') {
				reader.read();
				return integer(builder, 16, Number::isHexDigit);
			} else if (peek == 'o') {
				reader.read();
				return integer(builder, 8, Number::isOctalDigit);
			} else if (peek == 'b') {
				reader.read();
				return integer(builder, 2, Number::isBinaryDigit);
			}
		}

		builder.appendCodePoint(firstDigit);
		integer(builder, Number::isDigit);
		var isDecimal = false;

		if (reader.peek() == '.') {
			reader.read();
			isDecimal = true;
			builder.append('.');
			if (!isDigit(reader.peek())) {
				throw new KDLParseException(reader.error("digit expected immediately after '.'", reader.line(), reader.column() + 1));
			}
			integer(builder, Number::isDigit);
		}

		if (reader.peek() == 'e' | reader.peek() == 'E') {
			isDecimal = true;
			builder.appendCodePoint(reader.read());
			if (isSign(reader.peek())) {
				builder.appendCodePoint(reader.read());
			}
			if (!isDigit(reader.peek())) {
				throw new KDLParseException(reader.error("digit expected at start of exponent", reader.line(), reader.column() + 1));
			}
			integer(builder, Number::isDigit);
		}

		return isDecimal
			? new Number.Decimal(new BigDecimal(builder.toString()))
			: new Number.Integer(new BigInteger(builder.toString()));
	}

	@Nonnull
	private Number.Integer integer(@Nonnull StringBuilder builder, int radix, @Nonnull Predicate<Integer> digitPredicate) throws IOException {
		if (!digitPredicate.test(reader.peek())) {
			throw new KDLParseException(reader.error("digit expected at start of number", reader.line(), reader.column() + 1));
		}
		integer(builder, digitPredicate);
		return new Number.Integer(new BigInteger(builder.toString(), radix));
	}

	private void integer(@Nonnull StringBuilder builder, @Nonnull Predicate<Integer> digitPredicate) throws IOException {
		while (true) {
			var c = reader.peek();
			if (digitPredicate.test(c)) {
				builder.appendCodePoint(reader.read());
			} else if (c == '_') {
				reader.read();
			} else {
				return;
			}
		}
	}

	private final KDLReader reader;
	private Token token;

	private static final int EOF = -1;
	private static final Map<Integer, Integer> ESCAPED_CHARACTERS = new HashMap<>();

	static {
		ESCAPED_CHARACTERS.put((int) '"', (int) '"');
		ESCAPED_CHARACTERS.put((int) '\\', (int) '\\');
		ESCAPED_CHARACTERS.put((int) 'b', (int) '\b');
		ESCAPED_CHARACTERS.put((int) 'f', (int) '\f');
		ESCAPED_CHARACTERS.put((int) 'r', (int) '\r');
		ESCAPED_CHARACTERS.put((int) 'n', (int) '\n');
		ESCAPED_CHARACTERS.put((int) 't', (int) '\t');
		ESCAPED_CHARACTERS.put((int) 's', (int) ' ');
	}

}
