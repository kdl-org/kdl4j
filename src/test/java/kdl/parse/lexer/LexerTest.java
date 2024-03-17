package kdl.parse.lexer;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
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
import kdl.parse.lexer.token.StringToken.IdentifierString;
import kdl.parse.lexer.token.StringToken.QuotedString;
import kdl.parse.lexer.token.Whitespace;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static kdl.parse.lexer.token.Brace.CLOSING_BRACE;
import static kdl.parse.lexer.token.Brace.OPENING_BRACE;
import static kdl.parse.lexer.token.Parentheses.CLOSING_PARENTHESES;
import static kdl.parse.lexer.token.Parentheses.OPENING_PARENTHESES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LexerTest {
	static Stream<Arguments> validTestCases() {
		return Stream.of(
			Arguments.of("", null),
			Arguments.of("\ufeff", Bom.INSTANCE),
			Arguments.of("\n", new Newline("\n")),
			Arguments.of("\r", new Newline("\r")),
			Arguments.of("\r\n", new Newline("\r\n")),
			Arguments.of("\u000C", new Newline("\u000C")),
			Arguments.of(" ", new Whitespace(' ')),
			Arguments.of("\t", new Whitespace('\t')),
			Arguments.of("//\na", new SingleLineComment("//\n"), new IdentifierString("a")),
			Arguments.of("//\r\na", new SingleLineComment("//\r\n"), new IdentifierString("a")),
			Arguments.of("/* hi!\n */a", new MultiLineComment("/* hi!\n */"), new IdentifierString("a")),
			Arguments.of("/* hi /* there */ everyone */", new MultiLineComment("/* hi /* there */ everyone */")),
			Arguments.of("/-", Slashdash.INSTANCE),
			Arguments.of("=", new EqualsSign('=')),
			Arguments.of("(", OPENING_PARENTHESES),
			Arguments.of(")", CLOSING_PARENTHESES),
			Arguments.of("{", OPENING_BRACE),
			Arguments.of("}", CLOSING_BRACE),
			Arguments.of(";", Semicolon.INSTANCE),
			Arguments.of("a", new IdentifierString("a")),
			Arguments.of("abc", new IdentifierString("abc")),
			Arguments.of("-", new IdentifierString("-")),
			Arguments.of("-abc", new IdentifierString("-abc")),
			Arguments.of(". ", new IdentifierString("."), new Whitespace(' ')),
			Arguments.of(".abc ", new IdentifierString(".abc"), new Whitespace(' ')),
			Arguments.of("+.abc", new IdentifierString("+.abc")),
			Arguments.of("\"\"", new QuotedString("")),
			Arguments.of("\"abc\"", new QuotedString("abc")),
			Arguments.of("\"\\\"\"", new QuotedString("\"")),
			Arguments.of("\"\\\\\"", new QuotedString("\\")),
			Arguments.of("\"\\b\"", new QuotedString("\b")),
			Arguments.of("\"\\f\"", new QuotedString("\f")),
			Arguments.of("\"\\r\"", new QuotedString("\r")),
			Arguments.of("\"\\n\"", new QuotedString("\n")),
			Arguments.of("\"\\t\"", new QuotedString("\t")),
			Arguments.of("\"\\u{1}\"", new QuotedString("\u0001")),
			Arguments.of("\"\\u{1234}\"", new QuotedString("ሴ")),
			Arguments.of("\"\\u{1F643}\"", new QuotedString("\uD83D\uDE43")),
			Arguments.of("\"a\\   \n\nb\\   c\\  \n\"", new QuotedString("abc")),
			Arguments.of("\"\nHello,\nWorld!\n\"", new QuotedString("Hello,\nWorld!")),
			Arguments.of("\"\n    Hello,\n    World!\n    \"", new QuotedString("Hello,\nWorld!")),
			Arguments.of("\"\n    Hello,\n\n      World!\n    \"", new QuotedString("Hello,\n\n  World!")),
			Arguments.of("#\"\"\"#", new QuotedString("\"")),
			Arguments.of("##\"Hello\\n\\r\\asd\"#world\"##", new QuotedString("Hello\\n\\r\\asd\"#world")),
			Arguments.of("#\"\nHello,\nWorld!\n\"#", new QuotedString("Hello,\nWorld!")),
			Arguments.of("####\"\n   Hello,\\n\n    World!\"###\n   \"####", new QuotedString("Hello,\\n\n World!\"###")),
			Arguments.of("###\"\"#\"##\"###", new QuotedString("\"#\"##")),
			Arguments.of("\\", new Escline("\\")),
			Arguments.of("\\   // single-line comment", new Escline("\\   // single-line comment")),
			Arguments.of("\\ /* multi-line\n comment */  // single-line comment", new Escline("\\ /* multi-line\n comment */  // single-line comment")),
			Arguments.of("\\   \na", new Escline("\\   \n"), new IdentifierString("a")),
			Arguments.of("#true", Boolean.TRUE),
			Arguments.of("#false", Boolean.FALSE),
			Arguments.of("#null", Null.INSTANCE),
			Arguments.of("#inf", Number.INFINITY),
			Arguments.of("#-inf", Number.MINUS_INFINITY),
			Arguments.of("#nan", Number.NAN),
			Arguments.of("123", new Number.Integer(BigInteger.valueOf(123))),
			Arguments.of("+123", new Number.Integer(BigInteger.valueOf(123))),
			Arguments.of("-123", new Number.Integer(BigInteger.valueOf(-123))),
			Arguments.of("-1_2_3", new Number.Integer(BigInteger.valueOf(-123))),
			Arguments.of("-_123", new IdentifierString("-_123")),
			Arguments.of("0x12", new Number.Integer(BigInteger.valueOf(18L))),
			Arguments.of("0x1_2", new Number.Integer(BigInteger.valueOf(18L))),
			Arguments.of("-0x12", new Number.Integer(BigInteger.valueOf(-18L))),
			Arguments.of("0o12", new Number.Integer(BigInteger.valueOf(10L))),
			Arguments.of("0o1_2", new Number.Integer(BigInteger.valueOf(10L))),
			Arguments.of("-0o12", new Number.Integer(BigInteger.valueOf(-10L))),
			Arguments.of("0b101", new Number.Integer(BigInteger.valueOf(5L))),
			Arguments.of("0b1_01", new Number.Integer(BigInteger.valueOf(5L))),
			Arguments.of("-0b101", new Number.Integer(BigInteger.valueOf(-5L))),
			Arguments.of("2.5", new Number.Decimal(new BigDecimal("2.5"))),
			Arguments.of("123.456", new Number.Decimal(new BigDecimal("123.456"))),
			Arguments.of("123_456.789", new Number.Decimal(new BigDecimal("123456.789"))),
			Arguments.of("-123.456", new Number.Decimal(new BigDecimal("-123.456"))),
			Arguments.of("123e3", new Number.Decimal(new BigDecimal("1.23E5"))),
			Arguments.of("1_2_3e3_", new Number.Decimal(new BigDecimal("1.23E5"))),
			Arguments.of("-123.456e-3", new Number.Decimal(new BigDecimal("-0.123456"))),
			Arguments.of("node (type/*hey*/)10", new IdentifierString("node"), new Whitespace(' '), OPENING_PARENTHESES, new IdentifierString("type"), new MultiLineComment("/*hey*/"), CLOSING_PARENTHESES, new Number.Integer(BigInteger.valueOf(10L)))
		);
	}

	@ParameterizedTest
	@MethodSource("validTestCases")
	void validLexerTest(String input, ArgumentsAccessor expectedTokens) throws Exception {
		try (var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))) {
			var lexer = new Lexer(inputStream);
			for (var i = 1; i < expectedTokens.size(); i++) {
				assertThat(lexer.next()).isEqualTo(expectedTokens.get(i, Token.class));
			}
			assertThat(lexer.next()).isNull();
		}
	}

	@ParameterizedTest
	@MethodSource("validTestCases")
	void peekTest(String input) throws Exception {
		try (var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))) {
			var lexer = new Lexer(inputStream);
			var peeked = lexer.peek();
			var read = lexer.next();

			assertThat(read).isEqualTo(peeked);
		}
	}

	static Stream<Arguments> errorTestCases() {
		return Stream.of(
			Arguments.of(
				"/* hi!",
				"Error line 1 - unexpected end of file inside multi-line comment:\n" +
				"/* hi!\n" +
				"     ▲\n" +
				"─────╯"
			),
			Arguments.of(
				"/ ",
				"Error line 1 - [/*-] expected after '/' but got ' ':\n" +
				"/ \n" +
				" ▲\n" +
				"─╯"
			),
			Arguments.of(
				"true",
				"Error line 1 - keyword used as identifier, use a quoted string instead:\n" +
				"true\n" +
				"▲\n" +
				"╯"
			),
			Arguments.of(
				"\"",
				"Error line 1 - unexpected end of file inside quoted string:\n" +
				"\"\n" +
				"▲\n" +
				"╯"
			),
			Arguments.of(
				"\"a\n\"",
				"Error line 2 - unexpected newline inside quoted string, escape it or use a multi-line quoted string:\n" +
				"\"\n" +
				"▲\n" +
				"╯"
			),
			Arguments.of(
				"\"\\a\"",
				"Error line 1 - invalid escape sequence '\\a':\n" +
				"\"\\a\"\n" +
				"  ▲\n" +
				"──╯"
			),
			Arguments.of(
				"\"\\u1337\"",
				"Error line 1 - '{' expected at start of unicode escape:\n" +
				"\"\\u1337\"\n" +
				"   ▲\n" +
				"───╯"
			),
			Arguments.of(
				"\"\\u{12g4}\"",
				"Error line 1 - unexpected character in unicode escape:\n" +
				"\"\\u{12g4}\"\n" +
				"      ▲\n" +
				"──────╯"
			),
			Arguments.of(
				"\"\\u{}\"",
				"Error line 1 - at least one digit is required for a unicode escape:\n" +
				"\"\\u{}\"\n" +
				"    ▲\n" +
				"────╯"
			),
			Arguments.of(
				"\"\\u{1234567}\"",
				"Error line 1 - '}' expected at end of unicode escape:\n" +
				"\"\\u{1234567}\"\n" +
				"          ▲\n" +
				"──────────╯"
			),
			Arguments.of(
				"\"\\u{Fff456}\"",
				"Error line 1 - invalid unicode value U+FFF456:\n" +
				"\"\\u{Fff456}\"\n" +
				"    ▲\n" +
				"────╯"
			),
			Arguments.of(
				"\"\n  ab\n  cde\"",
				"Error line 3 - last line of a multi-line string must only contain whitespaces:\n" +
				"  cde\"\n" +
				"  ▲\n" +
				"──╯"
			),
			Arguments.of(
				"\"\n  ab\n cde\n  \"",
				"Error line 3 - multi-line string indentation must match last line (\"  \"):\n" +
				" cde\n" +
				"▲\n" +
				"╯"
			),
			Arguments.of(
				"\"\n   ab\n c\n   \"",
				"Error line 3 - multi-line string indentation must match last line (\"   \"):\n" +
				" c\n" +
				"▲\n" +
				"╯"
			),
			Arguments.of(
				"##abc##",
				"Error line 1 - a quote is required to start a raw string:\n" +
				"##abc##\n" +
				"  ▲\n" +
				"──╯"
			),
			Arguments.of(
				"#\"",
				"Error line 1 - unexpected end of file inside raw string:\n" +
				"#\"\n" +
				" ▲\n" +
				"─╯"
			),
			Arguments.of(
				"#\"\n",
				"Error line 2 - unexpected end of file inside raw string:\n" +
				"\n" +
				"▲\n" +
				"╯"
			),
			Arguments.of(
				"##\"a\"#\n\"##",
				"Error line 2 - unexpected newline inside raw string, use a multi-line raw string:\n" +
				"\"##\n" +
				"▲\n" +
				"╯"
			),
			Arguments.of(
				"\\ /a",
				"Error line 1 - invalid character in escaped line:\n" +
				"\\ /a\n" +
				"   ▲\n" +
				"───╯"
			),
			Arguments.of(
				"\\ a",
				"Error line 1 - invalid character in escaped line:\n" +
				"\\ a\n" +
				"  ▲\n" +
				"──╯"
			),
			Arguments.of(
				"#abc#",
				"Error line 1 - invalid value #abc:\n" +
				"#abc#\n" +
				"▲\n" +
				"╯"
			),
			Arguments.of(
				"0bx01",
				"Error line 1 - digit expected at start of number:\n" +
				"0bx01\n" +
				"  ▲\n" +
				"──╯"
			),
			Arguments.of(
				"0b701",
				"Error line 1 - digit expected at start of number:\n" +
				"0b701\n" +
				"  ▲\n" +
				"──╯"
			),
			Arguments.of(
				"2.",
				"Error line 1 - digit expected immediately after '.':\n" +
				"2.\n" +
				"  ▲\n" +
				"──╯"
			),
			Arguments.of(
				"2._",
				"Error line 1 - digit expected immediately after '.':\n" +
				"2._\n" +
				"  ▲\n" +
				"──╯"
			),
			Arguments.of(
				".0",
				"Error line 1 - invalid character '.':\n" +
				".0\n" +
				"▲\n" +
				"╯"
			),
			Arguments.of(
				"1._0",
				"Error line 1 - digit expected immediately after '.':\n" +
				"1._0\n" +
				"  ▲\n" +
				"──╯"
			),
			Arguments.of(
				"1.0e_3",
				"Error line 1 - digit expected at start of exponent:\n" +
				"1.0e_3\n" +
				"    ▲\n" +
				"────╯"
			),
			Arguments.of(
				"0x_10",
				"Error line 1 - digit expected at start of number:\n" +
				"0x_10\n" +
				"  ▲\n" +
				"──╯"
			)
		);
	}

	@ParameterizedTest
	@MethodSource("errorTestCases")
	void errorLexerTest(String input, String expectedMessage) throws Exception {
		try (var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))) {
			var lexer = new Lexer(inputStream);
			assertThatThrownBy(lexer::next)
				.isInstanceOf(KDLParseException.class)
				.hasMessage(expectedMessage);
		}
	}
}
