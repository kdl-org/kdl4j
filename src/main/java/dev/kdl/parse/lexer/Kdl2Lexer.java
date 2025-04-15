package dev.kdl.parse.lexer;

import dev.kdl.parse.KdlParseException;
import dev.kdl.parse.context.Position;
import dev.kdl.parse.context.Span;
import dev.kdl.parse.lexer.helper.IntegerBase;
import dev.kdl.parse.lexer.helper.Kdl2CharHelper;
import dev.kdl.parse.lexer.helper.KdlCharHelper;
import dev.kdl.parse.lexer.reader.KdlReader;
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static dev.kdl.parse.lexer.helper.Kdl2CharHelper.isDisallowedIdentifier;
import static dev.kdl.parse.lexer.helper.Kdl2CharHelper.isIdentifierChar;
import static dev.kdl.parse.lexer.helper.Kdl2CharHelper.isNewline;
import static dev.kdl.parse.lexer.helper.Kdl2CharHelper.isUnambiguousIdentifierChar;
import static dev.kdl.parse.lexer.helper.Kdl2CharHelper.isWhitespace;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.CR;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.LF;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.isDecimalDigit;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.isHexadecimalDigit;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.isSign;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.isUnicodeScalarValue;
import static dev.kdl.parse.lexer.reader.KdlReader.EOF;
import static java.util.function.Predicate.not;

public class Kdl2Lexer extends AbstractKdlLexer {

	public Kdl2Lexer(@Nullable String filename, @Nonnull InputStream inputStream, int capacity) {
		super(filename, new KdlReader(inputStream, READER_CAPACITY, Kdl2Lexer::isInvalid), capacity);
	}

	@Override
	protected Token nextToken() throws IOException, KdlParseException {
		var c = peekChar();

		switch (c) {
			case EOF:
				return null;
			case ByteOrderMark.VALUE:
				return consumeAndCreate(ByteOrderMark::new);
			case '=':
				return consumeAndCreate(EqualsSign::new);
			case '(':
				return consumeAndCreate(OpeningParentheses::new);
			case ')':
				return consumeAndCreate(ClosingParentheses::new);
			case '{':
				return consumeAndCreate(OpeningBrace::new);
			case '}':
				return consumeAndCreate(ClosingBrace::new);
			case ';':
				return consumeAndCreate(Semicolon::new);
			case '/': {
				var second = peekChar(1);
				if (second == '/') {
					return singleLineComment();
				} else if (second == '*') {
					return nodeSpace();
				} else if (second == '-') {
					return slashdash();
				}
				consumeChar();
				throw new KdlParseException(
					"Unexpected character after '/'",
					getErrorParseContext(Span.of(sourceLines.getNextPosition())),
					"'/', '*', or '-' expected here"
				);
			}
			case '"':
				return quotedString();
			case '#': {
				var second = peekChar(1);
				return second == '#' || second == '"'
					? rawString()
					: keyword();
			}
			case '\\':
				return nodeSpace();
		}

		if (isNewline(c)) {
			return newline();
		} else if (isWhitespace(c)) {
			return nodeSpace();
		} else if (isDecimalDigit(c) || isSign(c) && isDecimalDigit(peekChar(1))) {
			return number();
		} else if (isIdentifierString()) {
			return identifierString();
		}

		if (c == '.') {
			throw new KdlParseException(
				"Number or identifier cannot start with '.'",
				getErrorParseContext(Span.of(sourceLines.getNextPosition())),
				"invalid character",
				"for a number add a zero before '.', for an identifier use quotes"
			);
		}

		throw new KdlParseException(
			"Invalid character",
			getErrorParseContext(Span.of(sourceLines.getNextPosition())),
			"invalid character"
		);
	}

	@Nonnull
	private Newline newline() throws IOException {
		var builder = new StringBuilder();
		var span = newline(builder);
		return new Newline(builder.toString(), span);
	}

	@Nonnull
	private Span newline(StringBuilder builder) throws IOException {
		var start = sourceLines.getNextPosition();

		var newline = readChar();
		builder.appendCodePoint(newline);
		if (newline == CR && peekChar() == LF) {
			builder.appendCodePoint(readChar());
		}

		sourceLines.newline();
		var end = sourceLines.getCurrentPosition();

		return new Span(start, end);
	}

	@Nonnull
	private NodeSpace nodeSpace() throws IOException, KdlParseException {
		var builder = new StringBuilder();
		var start = sourceLines.getNextPosition();

		nodeSpace(builder);

		return new NodeSpace(builder.toString(), new Span(start, sourceLines.getCurrentPosition()));
	}

	private boolean nodeSpace(StringBuilder builder) throws IOException, KdlParseException {
		boolean hasReadCharacters = false;

		while (true) {
			var c = peekChar(0);

			if (isWhitespace(c)) {
				hasReadCharacters = true;
				builder.appendCodePoint(readChar());
			} else if (c == '\\') {
				hasReadCharacters = true;
				lineContinuation(builder);
			} else if (c == '/' && peekChar(1) == '*') {
				multilineComment(builder);
			} else {
				break;
			}
		}

		return hasReadCharacters;
	}

	private void lineContinuation(StringBuilder builder) throws IOException, KdlParseException {
		builder.appendCodePoint(readChar());

		while (true) {
			var c = peekChar(0);
			if (isWhitespace(c)) {
				builder.appendCodePoint(readChar());
			} else if (c == '/') {
				var second = peekChar(1);
				if (second == '/') {
					singleLineComment(builder);
					break;
				} else if (second == '*') {
					multilineComment(builder);
				} else {
					consumeChar();
					throw new KdlParseException(
						"Unexpected character after '/'",
						getErrorParseContext(Span.of(sourceLines.getNextPosition())),
						"'/' or '*' expected here"
					);
				}
			} else if (isNewline(c)) {
				builder.appendCodePoint(readChar());
				if (c == CR && peekChar() == LF) {
					builder.appendCodePoint(readChar());
				}
				sourceLines.newline();
				break;
			} else if (c == EOF) {
				break;
			} else {
				throw new KdlParseException(
					"Unexpected character in line continuation",
					getErrorParseContext(Span.of(sourceLines.getNextPosition())),
					"unexpected character",
					"a line continuation can only contain whitespaces or comments"
				);
			}
		}
	}

	private void multilineComment(StringBuilder builder) throws IOException, KdlParseException {
		consumeChar(2);
		builder.append("/*");

		var expectedEnds = 1;

		while (true) {
			var c = peekChar();
			if (c == EOF) {
				throw new KdlParseException(
					"Unexpected end of file in multi-line comment",
					getErrorParseContext(Span.of(sourceLines.getNextPosition())),
					"end of file"
				);
			} else if (isNewline(c)) {
				newline(builder);
			} else {
				consumeChar();
				builder.appendCodePoint(c);
			}
			if (c == '/' && peekChar() == '*') {
				expectedEnds += 1;
			}
			if (c == '*' && peekChar() == '/') {
				consumeChar();
				builder.append('/');
				expectedEnds -= 1;
				if (expectedEnds == 0) {
					break;
				}
			}
		}
	}

	@Nonnull
	private SingleLineComment singleLineComment() throws IOException {
		var builder = new StringBuilder();
		var start = sourceLines.getNextPosition();

		singleLineComment(builder);

		return new SingleLineComment(builder.toString(), new Span(start, sourceLines.getCurrentPosition()));
	}

	private void singleLineComment(StringBuilder builder) throws IOException {
		consumeChar(2);
		builder.append("//");

		while (true) {
			var c = peekChar();
			if (isNewline(c)) {
				newline(builder);
				break;
			} else if (c == EOF) {
				break;
			}
			builder.appendCodePoint(readChar());
		}
	}

	@Nonnull
	private Slashdash slashdash() throws IOException, KdlParseException {
		var builder = new StringBuilder("/-");
		var start = sourceLines.getNextPosition();
		consumeChar(2);

		while (true) {
			if (!nodeSpace(builder)) {
				var c = peekChar(0);
				if (isNewline(c)) {
					newline(builder);
				} else if (c == '/') {
					var c2 = peekChar(1);
					if (c2 != '/') {
						consumeChar();
						throw new KdlParseException(
							"Unexpected character after '/'",
							getErrorParseContext(Span.of(sourceLines.getNextPosition())),
							"'/' or '*' expected here"
						);
					}
					singleLineComment(builder);
				} else {
					break;
				}
			}
		}

		return new Slashdash(builder.toString(), new Span(start, sourceLines.getCurrentPosition()));
	}

	@Nonnull
	private StringToken quotedString() throws IOException, KdlParseException {
		if (peekChar(1) == '"') {
			if (peekChar(2) == '"') {
				return multiLineQuotedString();
			}
		}
		return singleLineQuotedString();
	}

	@Nonnull
	private StringToken multiLineQuotedString() throws IOException, KdlParseException {
		var start = sourceLines.getNextPosition();
		consumeChar(3);
		expectNewLine();

		var lines = new ArrayList<MultilineStringLine>();

		while (true) {
			var prefix = readPrefix();
			var content = new StringBuilder();
			if (readContent(start.line(), content)) {
				lines.add(new MultilineStringLine(prefix, content.toString()));
			} else {
				if (!content.isEmpty()) {
					int line = sourceLines.getCurrentPosition().line();
					throw new KdlParseException(
						"Unexpected character in last line of multi-line string",
						getErrorParseContext(start.line(), line, Span.of(line, prefix.length() + 1)),
						"unexpected character",
						"the last line of a multi-line string must only contain whitespaces"
					);
				}
				var span = new Span(start, sourceLines.getCurrentPosition());
				return new StringToken(getMultilineStringValue(prefix, lines, span), span);
			}
		}
	}

	private String readPrefix() throws IOException {
		var prefix = new StringBuilder();

		while (true) {
			var c = peekChar();
			if (!isWhitespace(c)) {
				break;
			}
			prefix.appendCodePoint(readChar());
		}
		return prefix.toString();
	}

	private boolean readContent(int startLine, StringBuilder content) throws IOException, KdlParseException {
		while (true) {
			var c = peekChar();
			if (c == EOF) {
				var nextPosition = sourceLines.getNextPosition();
				throw new KdlParseException(
					"Unexpected end of file in string",
					getErrorParseContext(startLine, nextPosition.line(), Span.of(nextPosition)),
					"end of file"
				);
			} else if (c == '"') {
				consumeChar();
				if (peekChar(0) == '"') {
					if (peekChar(1) == '"') {
						consumeChar(2);
						return false;
					}
				}
				content.append('"');
			} else if (isNewline(c)) {
				consumeNewLine();
				return true;
			} else {
				stringCharacter(content);
			}
		}
	}

	@Nonnull
	private String getMultilineStringValue(@Nonnull String lastLinePrefix, @Nonnull List<MultilineStringLine> lines, @Nonnull Span span) throws IOException, KdlParseException {
		var builder = new StringBuilder();

		for (var i = 0; i < lines.size(); i++) {
			builder.append(removeIndent(span, lastLinePrefix, span.start().line() + i + 1, lines.get(i)));
			if (i < lines.size() - 1) {
				builder.append('\n');
			}
		}

		return builder.toString();
	}

	@Nonnull
	private String removeIndent(@Nonnull Span span, @Nonnull String lastLinePrefix, int lineNumber, @Nonnull MultilineStringLine line) throws IOException, KdlParseException {
		if (line.content.isEmpty()) {
			return "";
		}

		if (!line.prefix.startsWith(lastLinePrefix)) {
			var errorSpan = new Span(new Position(lineNumber, 1), new Position(lineNumber, Math.max(1, line.prefix.length())));
			throw new KdlParseException(
				"Invalid indentation in multi-line string",
				getErrorParseContext(span.start().line(), span.end().line(), errorSpan),
				"indentation does not match last line"
			);
		}

		return line.prefix.length() > lastLinePrefix.length()
			? line.prefix.substring(lastLinePrefix.length()) + line.content
			: line.content;
	}

	private void expectNewLine() throws IOException, KdlParseException {
		if (!isNewline(peekChar())) {
			throw new KdlParseException(
				"Missing newline at start of multi-line string",
				getErrorParseContext(Span.of(sourceLines.getNextPosition())),
				"newline expected"
			);
		}
		consumeNewLine();
	}

	private void consumeNewLine() throws IOException {
		if (readChar() == CR && peekChar() == LF) {
			consumeChar();
		}
		sourceLines.newline();
	}

	private boolean isWhitespaces(@Nonnull String line) {
		return line.chars().allMatch(Kdl2CharHelper::isWhitespace);
	}

	private int countSpaces(String line) {
		return (int) line.chars().takeWhile(Kdl2CharHelper::isWhitespace).count();
	}

	@Nonnull
	private StringToken singleLineQuotedString() throws IOException, KdlParseException {
		var builder = new StringBuilder();
		var start = sourceLines.getNextPosition();
		consumeChar();

		while (true) {
			var c = peekChar();
			if (c == EOF) {
				throw new KdlParseException(
					"Unexpected end of file in string",
					getErrorParseContext(Span.of(sourceLines.getNextPosition())),
					"end of file"
				);
			} else if (c == '"') {
				consumeChar();
				break;
			} else if (isNewline(c)) {
				throw new KdlParseException(
					"Unexpected new line in string",
					getErrorParseContext(Span.of(sourceLines.getNextPosition())),
					"new line",
					"escape it or use a multi-line string"
				);
			} else {
				stringCharacter(builder);
			}
		}

		return new StringToken(builder.toString(), new Span(start, sourceLines.getCurrentPosition()));
	}

	private void stringCharacter(@Nonnull StringBuilder builder) throws IOException, KdlParseException {
		var c = readChar();
		if (c == '\\') {
			escapedCharacter(builder);
		} else {
			builder.appendCodePoint(c);
		}
	}

	private void escapedCharacter(@Nonnull StringBuilder builder) throws IOException, KdlParseException {
		var c = peekChar();
		var escapedCharacter = ESCAPED_CHARACTERS.get(c);
		if (escapedCharacter != null) {
			consumeChar();
			builder.appendCodePoint(escapedCharacter);
		} else if (c == 'u') {
			consumeChar();
			builder.appendCodePoint(unicodeEscape());
		} else if (isWhitespace(c) || isNewline(c)) {
			whitespaceEscape();
		} else {
			throw new KdlParseException(
				"Invalid escaped character '" + (char) c + "'",
				getErrorParseContext(Span.of(sourceLines.getNextPosition())),
				"'\"', '\\', 'b', 'f', 'r', 'n', 't', or 's' expected"
			);
		}
	}

	private int unicodeEscape() throws IOException, KdlParseException {
		if (readChar() != '{') {
			throw new KdlParseException(
				"Unicode escape without '{'",
				getErrorParseContext(Span.of(sourceLines.getCurrentPosition())),
				"missing '{' here"
			);
		}

		var hexValue = new StringBuilder();
		while (true) {
			var c = readChar();
			if (c == '}') {
				break;
			} else if (isHexadecimalDigit(c)) {
				hexValue.appendCodePoint(c);
			} else if (c == EOF) {
				throw new KdlParseException(
					"Unexpected end of file in Unicode escape",
					getErrorParseContext(Span.of(sourceLines.getNextPosition())),
					"end of file"
				);
			} else {
				throw new KdlParseException(
					"Unexpected character in Unicode escape",
					getErrorParseContext(Span.of(sourceLines.getCurrentPosition())),
					"invalid character"
				);
			}
		}

		if (hexValue.isEmpty() || hexValue.length() > 6) {
			var end = sourceLines.getCurrentPosition();
			var start = end.withColumnOffset(-hexValue.length() - 3);
			throw new KdlParseException(
				"Invalid Unicode escape",
				getErrorParseContext(new Span(start, end)),
				"invalid Unicode escape",
				"a unicode escape must have between 1 and 6 hexadecimal digits"
			);
		}

		var codePoint = Integer.parseInt(hexValue.toString(), 16);
		if (!isUnicodeScalarValue(codePoint)) {
			var currentPosition = sourceLines.getCurrentPosition();
			var start = currentPosition.withColumnOffset(-hexValue.length());
			var end = currentPosition.withColumnOffset(-1);
			throw new KdlParseException(
				"Invalid Unicode escape",
				getErrorParseContext(new Span(start, end)),
				"invalid Unicode scalar",
				"a Unicode escape must contain a valid Unicode scalar value in hexadecimal characters"
			);
		}

		return codePoint;
	}

	private void whitespaceEscape() throws IOException {
		while (true) {
			var c = peekChar();
			if (!isWhitespace(c) && !isNewline(c)) {
				return;
			}

			consumeChar();

			if (isNewline(c)) {
				if (c == CR && peekChar() == LF) {
					consumeChar();
				}
				sourceLines.newline();
			}
		}
	}

	@Nonnull
	private StringToken rawString() throws IOException, KdlParseException {
		var start = sourceLines.getNextPosition();
		var openingSharpSigns = 0;
		while (peekChar() == '#') {
			consumeChar();
			openingSharpSigns += 1;
		}

		if (readChar() != '"') {
			throw new KdlParseException(
				"Raw string is missing opening quotes",
				getErrorParseContext(Span.of(sourceLines.getCurrentPosition())),
				"missing '\"'"
			);
		}

		if (peekChar(0) == '"') {
			if (peekChar(1) == '"') {
				if (!isNewline(peekChar(2))) {
					consumeChar(2);
					throw new KdlParseException(
						"Newline required after opening quotes in multi-line raw string",
						getErrorParseContext(Span.of(sourceLines.getNextPosition())),
						"new-line expected"
					);
				}
				return multiLineRawString(start, openingSharpSigns);
			}
		}

		return singleLineRawString(start, openingSharpSigns);
	}

	@Nonnull
	private StringToken multiLineRawString(@Nonnull Position start, int openingSharpSigns) throws IOException, KdlParseException {
		consumeChar(2);
		expectNewLine();

		var lines = new ArrayList<String>();
		var currentLine = new StringBuilder();

		while (true) {
			var c = readChar();
			if (c == EOF) {
				var nextPosition = sourceLines.getNextPosition();
				throw new KdlParseException(
					"Unexpected end of file in raw string",
					getErrorParseContext(start.line(), nextPosition.line(), Span.of(nextPosition)),
					"end of file"
				);
			} else if (isNewline(c)) {
				sourceLines.newline();
				lines.add(currentLine.toString());
				currentLine.setLength(0);
			} else if (c == '"' && peekChar(0) == '"' && peekChar(1) == '"') {
				consumeChar(2);
				var closingSharpSigns = 0;
				while (peekChar() == '#' && closingSharpSigns < openingSharpSigns) {
					consumeChar();
					closingSharpSigns += 1;
				}
				if (closingSharpSigns == openingSharpSigns) {
					break;
				}
				currentLine.append("\"\"\"");
				writeNonClosingSharpSigns(currentLine, closingSharpSigns);
			} else {
				currentLine.appendCodePoint(c);
			}
		}

		var lastLine = currentLine.toString();
		var span = new Span(start, sourceLines.getCurrentPosition());
		return new StringToken(getMultiLineRawStringValue(lines, lastLine, span), span);
	}

	@Nonnull
	private String getMultiLineRawStringValue(@Nonnull List<String> lines, @Nonnull String lastLine, @Nonnull Span span) throws IOException, KdlParseException {
		checkLastLine(span, lastLine);
		var builder = new StringBuilder();

		for (var i = 0; i < lines.size(); i++) {
			builder.append(removeIndent(span, span.start().line() + i + 1, lines.get(i), lastLine));
			if (i < lines.size() - 1) {
				builder.append('\n');
			}
		}

		return builder.toString();
	}

	private void checkLastLine(@Nonnull Span span, @Nonnull String lastLine) throws IOException, KdlParseException {
		for (var column = 0; column < lastLine.length(); column++) {
			if (!isWhitespace(lastLine.charAt(column))) {
				var errorSpan = Span.of(new Position(span.end().line(), column + 1));
				throw new KdlParseException(
					"Unexpected character in last line of multi-line string",
					getErrorParseContext(span.start().line(), span.end().line(), errorSpan),
					"unexpected character",
					"the last line of a multi-line string must only contain whitespaces"
				);
			}
		}
	}

	@Nonnull
	private String removeIndent(@Nonnull Span span, int lineNumber, @Nonnull String line, @Nonnull String lastLine) throws IOException, KdlParseException {
		if (isWhitespaces(line)) {
			return "";
		}
		if (!line.startsWith(lastLine)) {
			var errorSpan = new Span(new Position(lineNumber, 1), new Position(lineNumber, Math.max(countSpaces(line), 1)));
			throw new KdlParseException(
				"Invalid indentation in multi-line string",
				getErrorParseContext(span.start().line(), span.end().line(), errorSpan),
				"indentation does not match last line"
			);
		}
		return line.substring(lastLine.length());
	}

	@Nonnull
	private StringToken singleLineRawString(@Nonnull Position start, int openingSharpSigns) throws IOException, KdlParseException {
		var builder = new StringBuilder();

		while (true) {
			var c = peekChar();
			if (c == EOF) {
				throw new KdlParseException(
					"Unexpected end of file in raw string",
					getErrorParseContext(Span.of(sourceLines.getNextPosition())),
					"end of file"
				);
			} else if (isNewline(c)) {
				throw new KdlParseException(
					"Unexpected new line in raw string",
					getErrorParseContext(Span.of(sourceLines.getNextPosition())),
					"new line"
				);
			} else if (c == '"' && peekChar(1) == '#') {
				consumeChar();
				var closingSharpSigns = 0;
				while (peekChar() == '#' && closingSharpSigns < openingSharpSigns) {
					consumeChar();
					closingSharpSigns += 1;
				}
				if (closingSharpSigns == openingSharpSigns) {
					return new StringToken(builder.toString(), new Span(start, sourceLines.getCurrentPosition()));
				}
				builder.append("\"");
				writeNonClosingSharpSigns(builder, closingSharpSigns);
			} else {
				consumeChar();
				builder.appendCodePoint(c);
			}
		}
	}

	private static void writeNonClosingSharpSigns(@Nonnull StringBuilder builder, int closingSharpSigns) {
		if (closingSharpSigns > 0) {
			builder.append("#".repeat(closingSharpSigns));
		}
	}

	@Nonnull
	private Token keyword() throws IOException, KdlParseException {
		var start = sourceLines.getNextPosition();
		consumeChar();
		var builder = new StringBuilder();

		while (isIdentifierChar(peekChar())) {
			builder.appendCodePoint(readChar());
		}

		var span = new Span(start, sourceLines.getCurrentPosition());
		var keyword = builder.toString();
		return switch (keyword) {
			case "true" -> new Boolean(true, span);
			case "false" -> new Boolean(false, span);
			case "null" -> new Null(span);
			case "inf" -> new Number.Infinity(span);
			case "-inf" -> new Number.NegativeInfinity(span);
			case "nan" -> new Number.NaN(span);
			default -> throw new KdlParseException(
				"Invalid keyword '#" + keyword + "'",
				getErrorParseContext(new Span(start, start.withColumnOffset(keyword.length()))),
				"unknown keyword"
			);
		};
	}

	@Nonnull
	private Number number() throws IOException, KdlParseException {
		var start = sourceLines.getNextPosition();
		var builder = new StringBuilder();

		if (isSign(peekChar())) {
			builder.appendCodePoint(readChar());
		}

		if (peekChar(0) == '0') {
			var peek = peekChar(1);
			if (peek == 'b') {
				return integer(start, builder, IntegerBase.BINARY);
			} else if (peek == 'o') {
				return integer(start, builder, IntegerBase.OCTAL);
			} else if (peek == 'x') {
				return integer(start, builder, IntegerBase.HEXADECIMAL);
			}
		}

		var isDecimal = false;
		parseDigits(builder);

		if (peekChar() == '.') {
			consumeChar();
			isDecimal = true;
			builder.append('.');
			checkNextCharacter(KdlCharHelper::isDecimalDigit, "number after decimal separator");
			parseDigits(builder);
		}

		if (peekChar() == 'e' | peekChar() == 'E') {
			isDecimal = true;
			builder.appendCodePoint(readChar());
			if (isSign(peekChar())) {
				builder.appendCodePoint(readChar());
			}
			checkNextCharacter(KdlCharHelper::isDecimalDigit, "number after exponential character");
			parseDigits(builder);
		}

		var span = new Span(start, sourceLines.getCurrentPosition());

		checkNextCharacter(not(Kdl2CharHelper::isIdentifierChar), "number");

		return isDecimal
			? decimal(span, builder.toString())
			: integer(span, builder.toString(), IntegerBase.DECIMAL);
	}

	private Number.Integer integer(
		@Nonnull Position start,
		@Nonnull StringBuilder builder,
		@Nonnull IntegerBase base
	) throws IOException, KdlParseException {
		consumeChar(2);

		if (!base.predicate().test(peekChar())) {
			throw new KdlParseException(
				"Integer must start with a digit",
				getErrorParseContext(Span.of(sourceLines.getNextPosition())),
				"digit expected here"
			);
		}

		parseDigits(builder, base.predicate());
		var span = new Span(start, sourceLines.getCurrentPosition());
		var number = builder.toString();

		checkNextCharacter(not(KdlCharHelper::isHexadecimalDigit), base);
		checkNextCharacter(not(Kdl2CharHelper::isIdentifierChar), "integer");

		return integer(span, number, base);
	}

	private void checkNextCharacter(Predicate<Integer> predicate, IntegerBase base) throws IOException, KdlParseException {
		checkNextCharacter(predicate, base.getName() + " integer", base.getName() + " digit expected");
	}

	private void checkNextCharacter(Predicate<Integer> predicate, String position) throws IOException, KdlParseException {
		checkNextCharacter(predicate, position, "digit expected");
	}

	private void checkNextCharacter(Predicate<Integer> predicate, String position, String label) throws IOException, KdlParseException {
		var nextChar = peekChar();
		if (!predicate.test(nextChar)) {
			throw new KdlParseException(
				(nextChar == EOF ? "Invalid character in " : "Invalid character '" + (char) nextChar + "' in ") + position,
				getErrorParseContext(Span.of(sourceLines.getNextPosition())),
				label
			);
		}
	}

	private void parseDigits(@Nonnull StringBuilder builder) throws IOException {
		parseDigits(builder, KdlCharHelper::isDecimalDigit);
	}

	private void parseDigits(@Nonnull StringBuilder builder, @Nonnull Predicate<Integer> digitPredicate) throws IOException {
		while (true) {
			var c = peekChar();
			if (digitPredicate.test(c)) {
				builder.appendCodePoint(readChar());
			} else if (c == '_') {
				consumeChar();
			} else {
				break;
			}
		}
	}

	private Number.Integer integer(@Nonnull Span span, @Nonnull String number, @Nonnull IntegerBase base) throws IOException, KdlParseException {
		try {
			return new Number.Integer(new BigInteger(number, base.radix()), span);
		} catch (NumberFormatException e) {
			throw invalidNumber(span, number, base);
		}
	}

	private Number.Decimal decimal(@Nonnull Span span, @Nonnull String number) throws IOException, KdlParseException {
		try {
			return new Number.Decimal(new BigDecimal(number), span);
		} catch (NumberFormatException e) {
			throw invalidNumber(span, number, IntegerBase.DECIMAL);
		}
	}

	private KdlParseException invalidNumber(@Nonnull Span span, @Nonnull String number, @Nonnull IntegerBase base) throws IOException {
		return new KdlParseException(
			"Invalid number " + base.getPrefix() + number,
			getErrorParseContext(span),
			"invalid number"
		);
	}

	private boolean isIdentifierString() throws IOException {
		var c = peekChar(0);
		if (isSign(c)) {
			var c2 = peekChar(1);
			if (c2 == '.') {
				var c3 = peekChar(2);
				return !isDecimalDigit(c3);
			}
			return !isDecimalDigit(c2);
		}
		if (c == '.') {
			var c2 = peekChar(1);
			return !isDecimalDigit(c2);
		}
		return isUnambiguousIdentifierChar(c);
	}

	@Nonnull
	private StringToken identifierString() throws IOException, KdlParseException {
		var builder = new StringBuilder();
		var start = sourceLines.getNextPosition();

		while (isIdentifierChar(peekChar())) {
			builder.appendCodePoint(readChar());
		}

		var next = peekChar();
		if (next == '#' || next == '"' || next == '(' || next == '[') {
			throw new KdlParseException(
				"Invalid character '" + (char) next + "' in identifier",
				getErrorParseContext(Span.of(sourceLines.getNextPosition())),
				"unexpected character"
			);
		}

		var stringValue = builder.toString();
		var span = new Span(start, sourceLines.getCurrentPosition());

		if (isDisallowedIdentifier(stringValue)) {
			throw new KdlParseException(
				"Keyword used as identifier",
				getErrorParseContext(span),
				"invalid identifier",
				"for the corresponding keyword use '#' (#" + stringValue + "), for an identifier use quotes (\"" + stringValue + "\")"
			);
		}

		return new StringToken(stringValue, span);
	}

	private static boolean isInvalid(int codepoint) {
		return codepoint <= 8
			|| (codepoint >= 0x000E && codepoint <= 0x01F)
			|| codepoint == 0x007F
			|| (codepoint >= 0xD800 && codepoint <= 0xDFFF)
			|| (codepoint >= 0x200E && codepoint <= 0x200F)
			|| (codepoint >= 0x202A && codepoint <= 0x202E)
			|| (codepoint >= 0x2066 && codepoint <= 0x2069);
	}

	private static final int READER_CAPACITY = 3;

	private static final Map<Integer, Integer> ESCAPED_CHARACTERS = Map.of(
		(int) '"', (int) '"',
		(int) '\\', (int) '\\',
		(int) 'b', (int) '\b',
		(int) 'f', (int) '\f',
		(int) 'r', (int) '\r',
		(int) 'n', (int) '\n',
		(int) 't', (int) '\t',
		(int) 's', (int) ' '
	);

	private record MultilineStringLine(String prefix, String content) {
	}

}
