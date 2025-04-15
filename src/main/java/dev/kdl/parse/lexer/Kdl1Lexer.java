package dev.kdl.parse.lexer;

import dev.kdl.parse.KdlParseException;
import dev.kdl.parse.context.Position;
import dev.kdl.parse.context.Span;
import dev.kdl.parse.lexer.helper.IntegerBase;
import dev.kdl.parse.lexer.helper.Kdl1CharHelper;
import dev.kdl.parse.lexer.helper.KdlCharHelper;
import dev.kdl.parse.lexer.reader.KdlReader;
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.Predicate;

import static dev.kdl.parse.lexer.helper.Kdl1CharHelper.isIdentifierChar;
import static dev.kdl.parse.lexer.helper.Kdl1CharHelper.isNewline;
import static dev.kdl.parse.lexer.helper.Kdl1CharHelper.isWhitespace;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.CR;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.LF;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.isDecimalDigit;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.isHexadecimalDigit;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.isSign;
import static dev.kdl.parse.lexer.helper.KdlCharHelper.isUnicodeScalarValue;
import static dev.kdl.parse.lexer.reader.KdlReader.EOF;
import static java.util.function.Predicate.not;

public class Kdl1Lexer extends AbstractKdlLexer {

	public Kdl1Lexer(@Nullable String filename, @Nonnull InputStream inputStream, int capacity) {
		super(filename, new KdlReader(inputStream, READER_CAPACITY, (c) -> c <= 0x08), capacity);
	}

	@Override
	protected Token nextToken() throws IOException, KdlParseException {
		var c = peekChar();

		switch (c) {
			case EOF:
				return null;
			case '=':
				return consumeAndCreate(EqualsSign::new);
			case '(':
				return consumeAndCreate(Parentheses.OpeningParentheses::new);
			case ')':
				return consumeAndCreate(Parentheses.ClosingParentheses::new);
			case '{':
				return consumeAndCreate(Brace.OpeningBrace::new);
			case '}':
				return consumeAndCreate(Brace.ClosingBrace::new);
			case ';':
				return consumeAndCreate(Semicolon::new);
			case '/': {
				var second = peekChar(1);
				if (second == '/') {
					return singleLineComment();
				} else if (second == '*') {
					return whitespace();
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
			case 'r': {
				var second = peekChar(1);
				if (second == '"' || second == '#') {
					return rawString();
				}
				return bareIdentifier();
			}
			case '\\':
				return lineContinuation();
		}

		if (isNewline(c)) {
			return newline();
		} else if (isWhitespace(c)) {
			return whitespace();
		} else if (isDecimalDigit(c) || isSign(c) && isDecimalDigit(peekChar(1))) {
			return number();
		} else if (isBareIdentifier()) {
			return bareIdentifier();
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
	private Whitespace whitespace() throws IOException, KdlParseException {
		var builder = new StringBuilder();
		var start = sourceLines.getNextPosition();

		whitespace(builder);

		return new Whitespace(builder.toString(), new Span(start, sourceLines.getCurrentPosition()));
	}

	private boolean whitespace(StringBuilder builder) throws IOException, KdlParseException {
		boolean hasReadCharacters = false;

		while (true) {
			var c = peekChar(0);

			if (isWhitespace(c)) {
				hasReadCharacters = true;
				builder.appendCodePoint(readChar());
			} else if (c == '/' && peekChar(1) == '*') {
				hasReadCharacters = true;
				multilineComment(builder);
			} else {
				break;
			}
		}

		return hasReadCharacters;
	}

	@Nonnull
	private LineContinuation lineContinuation() throws IOException, KdlParseException {
		var builder = new StringBuilder();
		var start = sourceLines.getNextPosition();

		lineContinuation(builder);

		return new LineContinuation(builder.toString(), new Span(start, sourceLines.getCurrentPosition()));
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
			if (!whitespace(builder)) {
				if (peekChar() == '\\') {
					lineContinuation(builder);
				} else {
					break;
				}
			}
		}

		return new Slashdash(builder.toString(), new Span(start, sourceLines.getCurrentPosition()));
	}

	@Nonnull
	private StringToken quotedString() throws IOException, KdlParseException {
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
			} else {
				consumeChar();
				if (c == '\\') {
					escapedCharacter(builder);
				} else {
					builder.appendCodePoint(c);
					if (isNewline(c)) {
						sourceLines.newline();
					}
				}
			}
		}

		return new StringToken(builder.toString(), new Span(start, sourceLines.getCurrentPosition()));
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

	@Nonnull
	private StringToken rawString() throws IOException, KdlParseException {
		var builder = new StringBuilder();
		var start = sourceLines.getNextPosition();
		consumeChar();
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

		while (true) {
			var c = peekChar();
			if (c == EOF) {
				throw new KdlParseException(
					"Unexpected end of file in raw string",
					getErrorParseContext(Span.of(sourceLines.getNextPosition())),
					"end of file"
				);
			} else if (c == '"') {
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
				if (isNewline(c)) {
					sourceLines.newline();
				}
			}
		}
	}

	private static void writeNonClosingSharpSigns(@Nonnull StringBuilder builder, int closingSharpSigns) {
		if (closingSharpSigns > 0) {
			builder.append("#".repeat(closingSharpSigns));
		}
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

		checkNextCharacter(not(Kdl1CharHelper::isIdentifierChar), "number");

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
		checkNextCharacter(not(Kdl1CharHelper::isIdentifierChar), "integer");

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

	private boolean isBareIdentifier() throws IOException {
		var c = peekChar(0);
		if (isSign(c)) {
			return !isDecimalDigit(peekChar(1));
		}
		return !isDecimalDigit(c) && isIdentifierChar(c);
	}

	@Nonnull
	private Token bareIdentifier() throws IOException, KdlParseException {
		var builder = new StringBuilder();
		var start = sourceLines.getNextPosition();

		while (isIdentifierChar(peekChar())) {
			builder.appendCodePoint(readChar());
		}

		var next = peekChar();
		if (next == '"' || next == '(' || next == '[') {
			throw new KdlParseException(
				"Invalid character '" + (char) next + "' in identifier",
				getErrorParseContext(Span.of(sourceLines.getNextPosition())),
				"unexpected character"
			);
		}

		var span = new Span(start, sourceLines.getCurrentPosition());
		return switch (builder.toString()) {
			case "true" -> new Boolean(true, span);
			case "false" -> new Boolean(false, span);
			case "null" -> new Null(span);
			default -> new BareIdentifier(builder.toString(), span);
		};
	}

	private static final int READER_CAPACITY = 2;

	private static final Map<Integer, Integer> ESCAPED_CHARACTERS = Map.of(
		(int) 'n', (int) '\n',
		(int) 'r', (int) '\r',
		(int) 't', (int) '\t',
		(int) '\\', (int) '\\',
		(int) '/', (int) '/',
		(int) '"', (int) '"',
		(int) 'b', (int) '\b',
		(int) 'f', (int) '\f'
	);
}
