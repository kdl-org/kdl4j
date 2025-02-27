package dev.kdl.parse.lexer;

import dev.kdl.parse.KdlParseException;
import dev.kdl.parse.Reporter;
import dev.kdl.parse.context.Span;
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
import dev.kdl.parse.lexer.token.StringToken.IdentifierString;
import dev.kdl.parse.lexer.token.StringToken.QuotedString;
import dev.kdl.parse.lexer.token.Token;
import jakarta.annotation.Nonnull;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Kdl2LexerTest {

	static Stream<Arguments> validTestCases() {
		return Stream.of(
			Arguments.of("", null),
			Arguments.of("\ufeff", new ByteOrderMark(Span.of(1, 1))),
			Arguments.of("\n", new Newline("\n", Span.of(1, 1))),
			Arguments.of("\r", new Newline("\r", Span.of(1, 1))),
			Arguments.of("\r\n", new Newline("\r\n", Span.of(1, 1, 1, 2))),
			Arguments.of("\u000C", new Newline("\u000C", Span.of(1, 1))),
			Arguments.of(" ", new NodeSpace(" ", Span.of(1, 1))),
			Arguments.of("\t", new NodeSpace("\t", Span.of(1, 1))),
			Arguments.of("\t ", new NodeSpace("\t ", Span.of(1, 1, 1, 2))),
			Arguments.of("/* hi!\n */a", new NodeSpace("/* hi!\n */", Span.of(1, 1, 2, 3)), new IdentifierString("a", Span.of(2, 4))),
			Arguments.of("  /* hi!\n */a", new NodeSpace("  /* hi!\n */", Span.of(1, 1, 2, 3)), new IdentifierString("a", Span.of(2, 4))),
			Arguments.of("/* hi /* there */ everyone */", new NodeSpace("/* hi /* there */ everyone */", Span.of(1, 1, 1, 29))),
			Arguments.of("/* hello\n girls/boys */", new NodeSpace("/* hello\n girls/boys */", Span.of(1, 1, 2, 14))),
			Arguments.of("\\", new NodeSpace("\\", Span.of(1, 1))),
			Arguments.of(" \\", new NodeSpace(" \\", Span.of(1, 1, 1, 2))),
			Arguments.of("\\\n   ", new NodeSpace("\\\n   ", Span.of(1, 1, 2, 3))),
			Arguments.of("\\\r\n   ", new NodeSpace("\\\r\n   ", Span.of(1, 1, 2, 3))),
			Arguments.of("\\   // single-line comment", new NodeSpace("\\   // single-line comment", Span.of(1, 1, 1, 26))),
			Arguments.of("\\ /* multi-line\n comment */  // single-line comment", new NodeSpace("\\ /* multi-line\n comment */  // single-line comment", Span.of(1, 1, 2, 35))),
			Arguments.of("\\   \na", new NodeSpace("\\   \n", Span.of(1, 1, 1, 5)), new IdentifierString("a", Span.of(2, 1))),
			Arguments.of("a  \\   \nb", new IdentifierString("a", Span.of(1, 1)), new NodeSpace("  \\   \n", Span.of(1, 2, 1, 8)), new IdentifierString("b", Span.of(2, 1))),
			Arguments.of("//\na", new SingleLineComment("//\n", Span.of(1, 1, 1, 3)), new IdentifierString("a", Span.of(2, 1))),
			Arguments.of("//\r\na", new SingleLineComment("//\r\n", Span.of(1, 1, 1, 4)), new IdentifierString("a", Span.of(2, 1))),
			Arguments.of("/-", new Slashdash("/-", Span.of(1, 1, 1, 2))),
			Arguments.of("/-   // comment\n\n  ", new Slashdash("/-   // comment\n\n  ", Span.of(1, 1, 3, 2))),
			Arguments.of("/-   /* multiline\ncomment */\n\n  ", new Slashdash("/-   /* multiline\ncomment */\n\n  ", Span.of(1, 1, 4, 2))),
			Arguments.of("=", new EqualsSign(Span.of(1, 1))),
			Arguments.of("(", new OpeningParentheses(Span.of(1, 1))),
			Arguments.of(")", new ClosingParentheses(Span.of(1, 1))),
			Arguments.of("{", new OpeningBrace(Span.of(1, 1))),
			Arguments.of("}", new ClosingBrace(Span.of(1, 1))),
			Arguments.of(";", new Semicolon(Span.of(1, 1))),
			Arguments.of("a", new IdentifierString("a", Span.of(1, 1))),
			Arguments.of("abc", new IdentifierString("abc", Span.of(1, 1, 1, 3))),
			Arguments.of("-", new IdentifierString("-", Span.of(1, 1))),
			Arguments.of("-abc", new IdentifierString("-abc", Span.of(1, 1, 1, 4))),
			Arguments.of(". ", new IdentifierString(".", Span.of(1, 1)), new NodeSpace(" ", Span.of(1, 2))),
			Arguments.of(".abc ", new IdentifierString(".abc", Span.of(1, 1, 1, 4)), new NodeSpace(" ", Span.of(1, 5))),
			Arguments.of("+.abc", new IdentifierString("+.abc", Span.of(1, 1, 1, 5))),
			Arguments.of("\"\"", new QuotedString("", Span.of(1, 1, 1, 2))),
			Arguments.of("\"abc\"", new QuotedString("abc", Span.of(1, 1, 1, 5))),
			Arguments.of("\"\\\"\"", new QuotedString("\"", Span.of(1, 1, 1, 4))),
			Arguments.of("\"\\\\\"", new QuotedString("\\", Span.of(1, 1, 1, 4))),
			Arguments.of("\"\\b\"", new QuotedString("\b", Span.of(1, 1, 1, 4))),
			Arguments.of("\"\\f\"", new QuotedString("\f", Span.of(1, 1, 1, 4))),
			Arguments.of("\"\\r\"", new QuotedString("\r", Span.of(1, 1, 1, 4))),
			Arguments.of("\"\\n\"", new QuotedString("\n", Span.of(1, 1, 1, 4))),
			Arguments.of("\"\\r\\n\"", new QuotedString("\r\n", Span.of(1, 1, 1, 6))),
			Arguments.of("\"\\t\"", new QuotedString("\t", Span.of(1, 1, 1, 4))),
			Arguments.of("\"\\u{1}\"", new QuotedString("\u0001", Span.of(1, 1, 1, 7))),
			Arguments.of("\"\\u{1234}\"", new QuotedString("ሴ", Span.of(1, 1, 1, 10))),
			Arguments.of("\"\\u{1F643}\"", new QuotedString("\uD83D\uDE43", Span.of(1, 1, 1, 11))),
			Arguments.of("\"\\u{100000}\"", new QuotedString("\uDBC0\uDC00", Span.of(1, 1, 1, 12))),
			Arguments.of("\"a\\   \n\nb\\   c\\  \n\"", new QuotedString("abc", Span.of(1, 1, 4, 1))),
			Arguments.of("\"a\\   \r\n\r\nb\\   c\\  \n\"", new QuotedString("abc", Span.of(1, 1, 4, 1))),
			Arguments.of("\"\"\"\nHello,\nWorld!\n\"\"\"", new QuotedString("Hello,\nWorld!", Span.of(1, 1, 4, 3))),
			Arguments.of("\"\"\"\r\nHello,\r\nWorld!\r\n\"\"\"", new QuotedString("Hello,\nWorld!", Span.of(1, 1, 4, 3))),
			Arguments.of("\"\"\"\nHello,\\n\nWorld!\n\"\"\"", new QuotedString("Hello,\n\nWorld!", Span.of(1, 1, 4, 3))),
			Arguments.of("\"\"\"\n    Hello,\n    World!\n    \"\"\"", new QuotedString("Hello,\nWorld!", Span.of(1, 1, 4, 7))),
			Arguments.of("\"\"\"\n    Hello,\n\n      World!\n    \"\"\"", new QuotedString("Hello,\n\n  World!", Span.of(1, 1, 5, 7))),
			Arguments.of("\"\"\"\n  ab\\   c\n  \"\"\"", new QuotedString("abc", Span.of(1, 1, 3, 5))),
			Arguments.of("\"\"\"\n\"\"abc\"\"\n\"\"\"", new QuotedString("\"\"abc\"\"", Span.of(1, 1, 3, 3))),
			Arguments.of("\"\"\"\na\\\\ b\n\"\"\"", new QuotedString("a\\ b", Span.of(1, 1, 3, 3))),
			Arguments.of("\"\"\"\n\\\"\"\"\n\"\"\"", new QuotedString("\"\"\"", Span.of(1, 1, 3, 3))),
			Arguments.of("\"\"\"\n  foo \\\nbar\n  baz\n  \\   \"\"\"", new QuotedString("foo bar\nbaz", Span.of(1, 1, 5, 9))),
			Arguments.of("\"\"\"\n\t  \n abc\n     \n \"\"\"", new QuotedString("\nabc\n", Span.of(1, 1, 5, 4))),
			Arguments.of("#\"\"abc\"\"#", new QuotedString("\"abc\"", Span.of(1, 1, 1, 9))),
			Arguments.of("##\"Hello\\n\\r\\asd\"#world\"##", new QuotedString("Hello\\n\\r\\asd\"#world", Span.of(1, 1, 1, 26))),
			Arguments.of("###\"\"#\"##\"###", new QuotedString("\"#\"##", Span.of(1, 1, 1, 13))),
			Arguments.of("#\"\"\"\nHello,\nWorld!\n\"\"\"#", new QuotedString("Hello,\nWorld!", Span.of(1, 1, 4, 4))),
			Arguments.of("####\"\"\"\n   Hello,\\n\n    World!\"###\n   \"\"\"####", new QuotedString("Hello,\\n\n World!\"###", Span.of(1, 1, 4, 10))),
			Arguments.of("##\"\"\"\n\"\"\"abc\"\"\"\n\"\"\"##", new QuotedString("\"\"\"abc\"\"\"", Span.of(1, 1, 3, 5))),
			Arguments.of("#true", new Boolean(true, Span.of(1, 1, 1, 5))),
			Arguments.of("#false", new Boolean(false, Span.of(1, 1, 1, 6))),
			Arguments.of("#null", new Null(Span.of(1, 1, 1, 5))),
			Arguments.of("#inf", new Number.Infinity(Span.of(1, 1, 1, 4))),
			Arguments.of("#-inf", new Number.NegativeInfinity(Span.of(1, 1, 1, 5))),
			Arguments.of("#nan", new Number.NaN(Span.of(1, 1, 1, 4))),
			Arguments.of("123", new Number.Integer(BigInteger.valueOf(123), Span.of(1, 1, 1, 3))),
			Arguments.of("+123", new Number.Integer(BigInteger.valueOf(123), Span.of(1, 1, 1, 4))),
			Arguments.of("-123", new Number.Integer(BigInteger.valueOf(-123), Span.of(1, 1, 1, 4))),
			Arguments.of("-1_2_3", new Number.Integer(BigInteger.valueOf(-123), Span.of(1, 1, 1, 6))),
			Arguments.of("-_123", new IdentifierString("-_123", Span.of(1, 1, 1, 5))),
			Arguments.of("0x12", new Number.Integer(BigInteger.valueOf(18L), Span.of(1, 1, 1, 4))),
			Arguments.of("0x1_2", new Number.Integer(BigInteger.valueOf(18L), Span.of(1, 1, 1, 5))),
			Arguments.of("-0x12", new Number.Integer(BigInteger.valueOf(-18L), Span.of(1, 1, 1, 5))),
			Arguments.of("0o12", new Number.Integer(BigInteger.valueOf(10L), Span.of(1, 1, 1, 4))),
			Arguments.of("0o1_2", new Number.Integer(BigInteger.valueOf(10L), Span.of(1, 1, 1, 5))),
			Arguments.of("-0o12", new Number.Integer(BigInteger.valueOf(-10L), Span.of(1, 1, 1, 5))),
			Arguments.of("0b101", new Number.Integer(BigInteger.valueOf(5L), Span.of(1, 1, 1, 5))),
			Arguments.of("0b1_01", new Number.Integer(BigInteger.valueOf(5L), Span.of(1, 1, 1, 6))),
			Arguments.of("-0b101", new Number.Integer(BigInteger.valueOf(-5L), Span.of(1, 1, 1, 6))),
			Arguments.of("2.5", new Number.Decimal(new BigDecimal("2.5"), Span.of(1, 1, 1, 3))),
			Arguments.of("123.456", new Number.Decimal(new BigDecimal("123.456"), Span.of(1, 1, 1, 7))),
			Arguments.of("123_456.789", new Number.Decimal(new BigDecimal("123456.789"), Span.of(1, 1, 1, 11))),
			Arguments.of("-123.456", new Number.Decimal(new BigDecimal("-123.456"), Span.of(1, 1, 1, 8))),
			Arguments.of("123e3", new Number.Decimal(new BigDecimal("1.23E5"), Span.of(1, 1, 1, 5))),
			Arguments.of("1_2_3e3_", new Number.Decimal(new BigDecimal("1.23E5"), Span.of(1, 1, 1, 8))),
			Arguments.of("-123.456e-3", new Number.Decimal(new BigDecimal("-0.123456"), Span.of(1, 1, 1, 11))),
			Arguments.of("123 123", new Number.Integer(BigInteger.valueOf(123), Span.of(1, 1, 1, 3)), new NodeSpace(" ", Span.of(1, 4)), new Number.Integer(BigInteger.valueOf(123), Span.of(1, 5, 1, 7))),
			Arguments.of("(type) node", new OpeningParentheses(Span.of(1, 1)), new IdentifierString("type", Span.of(1, 2, 1, 5)), new ClosingParentheses(Span.of(1, 6)), new NodeSpace(" ", Span.of(1, 7)), new IdentifierString("node", Span.of(1, 8, 1, 11))),
			Arguments.of("node 0xabcdef1234567890", new IdentifierString("node", Span.of(1, 1, 1, 4)), new NodeSpace(" ", Span.of(1, 5)), new Number.Integer(new BigInteger("abcdef1234567890", 16), Span.of(1, 6, 1, 23)))
		);
	}

	@ParameterizedTest
	@MethodSource("validTestCases")
	void validLexerTest(String input, ArgumentsAccessor expectedTokens) throws Exception {
		try (var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))) {
			var lexer = new Kdl2Lexer("test.kdl", inputStream, 1);
			for (var i = 1; i < expectedTokens.size(); i++) {
				var token = lexer.read();
				assertThat(token).isEqualTo(expectedTokens.get(i));
			}
			assertThat(lexer.read()).isNull();
		}
	}

	@ParameterizedTest
	@MethodSource("validTestCases")
	void peekTest(String input) throws Exception {
		try (var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))) {
			var lexer = new Kdl2Lexer("test.kdl", inputStream, 1);
			var peeked = lexer.peek();
			var read = lexer.read();

			assertThat(read).isEqualTo(peeked);
		}
	}

	static Stream<Arguments> errorTestCases() {
		return Stream.of(
			Arguments.of(
				"/* hi!",
				"""
					× Unexpected end of file in multi-line comment:
					  ╭─[test.kdl:1:7]
					1 │ /* hi!
					  ·       ┬
					  ·       ╰ end of file
					  ╰─"""
			),
			Arguments.of(
				"/ ",
				"""
					× Unexpected character after '/':
					  ╭─[test.kdl:1:2]
					1 │ /
					  ·  ┬
					  ·  ╰ '/', '*', or '-' expected here
					  ╰─"""
			),
			Arguments.of(
				"true",
				"""
					× Keyword used as identifier:
					  ╭─[test.kdl:1:1]
					1 │ true
					  · ─┬──
					  ·  ╰ invalid identifier
					  ╰─
					help: for the corresponding keyword use '#' (#true), for an identifier use quotes ("true")"""
			),
			Arguments.of(
				"\"",
				"""
					× Unexpected end of file in string:
					  ╭─[test.kdl:1:2]
					1 │ "
					  ·  ┬
					  ·  ╰ end of file
					  ╰─"""
			),
			Arguments.of(
				"\"a\n\"",
				"""
					× Unexpected new line in string:
					  ╭─[test.kdl:1:3]
					1 │ "a
					  ·   ┬
					  ·   ╰ new line
					  ╰─
					help: escape it or use a multi-line string"""
			),
			Arguments.of(
				"\"\\u1337\"",
				"""
					× Unicode escape without '{':
					  ╭─[test.kdl:1:4]
					1 │ "\\u1337"
					  ·    ┬
					  ·    ╰ missing '{' here
					  ╰─"""
			),
			Arguments.of(
				"\"\\u{12g4}\"",
				"""
					× Unexpected character in Unicode escape:
					  ╭─[test.kdl:1:7]
					1 │ "\\u{12g4}"
					  ·       ┬
					  ·       ╰ invalid character
					  ╰─"""
			),
			Arguments.of(
				"\"\\u{1234\"",
				"""
					× Unexpected character in Unicode escape:
					  ╭─[test.kdl:1:9]
					1 │ "\\u{1234"
					  ·         ┬
					  ·         ╰ invalid character
					  ╰─"""
			),
			Arguments.of(
				"\"\\u{}\"",
				"""
					× Invalid Unicode escape:
					  ╭─[test.kdl:1:2]
					1 │ "\\u{}"
					  ·  ─┬──
					  ·   ╰ invalid Unicode escape
					  ╰─
					help: a unicode escape must have between 1 and 6 hexadecimal digits"""
			),
			Arguments.of(
				"\"\\u{123",
				"""
					× Unexpected end of file in Unicode escape:
					  ╭─[test.kdl:1:8]
					1 │ "\\u{123
					  ·        ┬
					  ·        ╰ end of file
					  ╰─"""
			),
			Arguments.of(
				"\"\\u{1234567}\"",
				"""
					× Invalid Unicode escape:
					  ╭─[test.kdl:1:2]
					1 │ "\\u{1234567}"
					  ·  ─────┬─────
					  ·       ╰ invalid Unicode escape
					  ╰─
					help: a unicode escape must have between 1 and 6 hexadecimal digits"""
			),
			Arguments.of(
				"\"\\u{D800}\"",
				"""
					× Invalid Unicode escape:
					  ╭─[test.kdl:1:5]
					1 │ "\\u{D800}"
					  ·     ─┬──
					  ·      ╰ invalid Unicode scalar
					  ╰─
					help: a Unicode escape must contain a valid Unicode scalar value in hexadecimal characters"""
			),
			Arguments.of(
				"\"\"\"\n  ab\n  cde\"\"\"",
				"""
					× Unexpected character in last line of multi-line string:
					  ╭─[test.kdl:3:3]
					1 │ ""\"
					2 │   ab
					3 │   cde""\"
					  ·   ┬
					  ·   ╰ unexpected character
					  ╰─
					help: the last line of a multi-line string must only contain whitespaces"""
			),
			Arguments.of(
				"#\"\"\"\n  ab\n  cde\"\"\"#",
				"""
					× Unexpected character in last line of multi-line string:
					  ╭─[test.kdl:3:3]
					1 │ #""\"
					2 │   ab
					3 │   cde""\"#
					  ·   ┬
					  ·   ╰ unexpected character
					  ╰─
					help: the last line of a multi-line string must only contain whitespaces"""
			),
			Arguments.of(
				"\"\"\"\n    ab\n   cd\n    \"\"\"",
				"""
					× Invalid indentation in multi-line string:
					  ╭─[test.kdl:3:1]
					1 │ ""\"
					2 │     ab
					3 │    cd
					  · ─┬─
					  ·  ╰ indentation does not match last line
					4 │     ""\"
					  ╰─"""
			),
			Arguments.of(
				"#\"\"\"\n    ab\n   cd\n    \"\"\"#",
				"""
					× Invalid indentation in multi-line string:
					  ╭─[test.kdl:3:1]
					1 │ #""\"
					2 │     ab
					3 │    cd
					  · ─┬─
					  ·  ╰ indentation does not match last line
					4 │     ""\"#
					  ╰─"""
			),
			Arguments.of(
				"##abc##",
				"""
					× Raw string is missing opening quotes:
					  ╭─[test.kdl:1:3]
					1 │ ##abc##
					  ·   ┬
					  ·   ╰ missing '"'
					  ╰─"""
			),
			Arguments.of(
				"#\"",
				"""
					× Unexpected end of file in raw string:
					  ╭─[test.kdl:1:3]
					1 │ #"
					  ·   ┬
					  ·   ╰ end of file
					  ╰─"""
			),
			Arguments.of(
				"##\"a\"#\n\"##",
				"""
					× Unexpected new line in raw string:
					  ╭─[test.kdl:1:7]
					1 │ ##"a"#
					  ·       ┬
					  ·       ╰ new line
					  ╰─"""
			),
			Arguments.of(
				"\\ /a",
				"""
					× Unexpected character after '/':
					  ╭─[test.kdl:1:4]
					1 │ \\ /a
					  ·    ┬
					  ·    ╰ '/' or '*' expected here
					  ╰─"""
			),
			Arguments.of(
				"\\ a",
				"""
					× Unexpected character in line continuation:
					  ╭─[test.kdl:1:3]
					1 │ \\ a
					  ·   ┬
					  ·   ╰ unexpected character
					  ╰─
					help: a line continuation can only contain whitespaces or comments"""
			),
			Arguments.of(
				"#abc#",
				"""
					× Invalid keyword '#abc':
					  ╭─[test.kdl:1:1]
					1 │ #abc#
					  · ─┬──
					  ·  ╰ unknown keyword
					  ╰─"""
			),
			Arguments.of(
				"#\"\"\"one line\"\"\"#",
				"""
					× Newline required after opening quotes in multi-line raw string:
					  ╭─[test.kdl:1:5]
					1 │ #""\"one line""\"#
					  ·     ┬
					  ·     ╰ new-line expected
					  ╰─"""
			),
			Arguments.of(
				"0bx01",
				"""
					× Integer must start with a digit:
					  ╭─[test.kdl:1:3]
					1 │ 0bx01
					  ·   ┬
					  ·   ╰ digit expected here
					  ╰─"""
			),
			Arguments.of(
				"0x1.3",
				"""
					× Invalid character '.' in integer:
					  ╭─[test.kdl:1:4]
					1 │ 0x1.3
					  ·    ┬
					  ·    ╰ digit expected
					  ╰─"""
			),
			Arguments.of(
				"1.",
				"""
					× Invalid character in number after decimal separator:
					  ╭─[test.kdl:1:3]
					1 │ 1.
					  ·   ┬
					  ·   ╰ digit expected
					  ╰─"""
			),
			Arguments.of(
				"1._",
				"""
					× Invalid character '_' in number after decimal separator:
					  ╭─[test.kdl:1:3]
					1 │ 1._
					  ·   ┬
					  ·   ╰ digit expected
					  ╰─"""
			),
			Arguments.of(
				"1._7",
				"""
					× Invalid character '_' in number after decimal separator:
					  ╭─[test.kdl:1:3]
					1 │ 1._7
					  ·   ┬
					  ·   ╰ digit expected
					  ╰─"""
			),
			Arguments.of(
				"1e",
				"""
					× Invalid character in number after exponential character:
					  ╭─[test.kdl:1:3]
					1 │ 1e
					  ·   ┬
					  ·   ╰ digit expected
					  ╰─"""
			),
			Arguments.of(
				"1.e7",
				"""
					× Invalid character 'e' in number after decimal separator:
					  ╭─[test.kdl:1:3]
					1 │ 1.e7
					  ·   ┬
					  ·   ╰ digit expected
					  ╰─"""
			),
			Arguments.of(
				"node .0n",
				"""
					× Number or identifier cannot start with '.':
					  ╭─[test.kdl:1:6]
					1 │ node .0n
					  ·      ┬
					  ·      ╰ invalid character
					  ╰─
					help: for a number add a zero before '.', for an identifier use quotes"""
			),
			Arguments.of(
				"0x",
				"""
					× Integer must start with a digit:
					  ╭─[test.kdl:1:3]
					1 │ 0x
					  ·   ┬
					  ·   ╰ digit expected here
					  ╰─"""
			),
			Arguments.of(
				"0x_10",
				"""
					× Integer must start with a digit:
					  ╭─[test.kdl:1:3]
					1 │ 0x_10
					  ·   ┬
					  ·   ╰ digit expected here
					  ╰─"""
			),
			Arguments.of(
				"0n",
				"""
					× Invalid character 'n' in number:
					  ╭─[test.kdl:1:2]
					1 │ 0n
					  ·  ┬
					  ·  ╰ digit expected
					  ╰─"""
			),
			Arguments.of(
				"0x10g10",
				"""
					× Invalid character 'g' in integer:
					  ╭─[test.kdl:1:5]
					1 │ 0x10g10
					  ·     ┬
					  ·     ╰ digit expected
					  ╰─"""
			),
			Arguments.of(
				"0b17",
				"""
					× Invalid character '7' in binary integer:
					  ╭─[test.kdl:1:4]
					1 │ 0b17
					  ·    ┬
					  ·    ╰ binary digit expected
					  ╰─"""
			),
			Arguments.of(
				"0o18",
				"""
					× Invalid character '8' in octal integer:
					  ╭─[test.kdl:1:4]
					1 │ 0o18
					  ·    ┬
					  ·    ╰ octal digit expected
					  ╰─"""
			),
			Arguments.of(
				"/- /-",
				"""
					× Unexpected character after '/':
					  ╭─[test.kdl:1:5]
					1 │ /- /-
					  ·     ┬
					  ·     ╰ '/' or '*' expected here
					  ╰─"""
			),
			Arguments.of(
				"\"oops",
				"""
					× Unexpected end of file in string:
					  ╭─[test.kdl:1:6]
					1 │ "oops
					  ·      ┬
					  ·      ╰ end of file
					  ╰─"""
			),
			Arguments.of(
				"\"\"\"\n  oops",
				"""
					× Unexpected end of file in string:
					  ╭─[test.kdl:2:7]
					1 │ ""\"
					2 │   oops
					  ·       ┬
					  ·       ╰ end of file
					  ╰─"""
			),
			Arguments.of(
				"\"w\\ot\"",
				"""
					× Invalid escaped character 'o':
					  ╭─[test.kdl:1:4]
					1 │ "w\\ot"
					  ·    ┬
					  ·    ╰ '"', '\\', 'b', 'f', 'r', 'n', 't', or 's' expected
					  ╰─"""
			),
			Arguments.of(
				"#\"\"\"\n   oops",
				"""
					× Unexpected end of file in raw string:
					  ╭─[test.kdl:2:8]
					1 │ #""\"
					2 │    oops
					  ·        ┬
					  ·        ╰ end of file
					  ╰─"""
			),
			Arguments.of(
				"abc#def",
				"""
					× Invalid character '#' in identifier:
					  ╭─[test.kdl:1:4]
					1 │ abc#def
					  ·    ┬
					  ·    ╰ unexpected character
					  ╰─"""
			),
			Arguments.of(
				"abc(def)",
				"""
					× Invalid character '(' in identifier:
					  ╭─[test.kdl:1:4]
					1 │ abc(def)
					  ·    ┬
					  ·    ╰ unexpected character
					  ╰─"""
			),
			Arguments.of(
				"\"\"\"one line\"\"\"",
				"""
					× Missing newline at start of multi-line string:
					  ╭─[test.kdl:1:4]
					1 │ ""\"one line""\"
					  ·    ┬
					  ·    ╰ newline expected
					  ╰─"""
			),
			Arguments.of(
				"""
					test
					""\"
					    \\g
					    ""\"
					""",
				"""
					× Invalid escaped character 'g':
					  ╭─[test.kdl:3:6]
					3 │     \\g
					  ·      ┬
					  ·      ╰ '"', '\\', 'b', 'f', 'r', 'n', 't', or 's' expected
					  ╰─"""
			)
		);
	}

	@ParameterizedTest
	@MethodSource("errorTestCases")
	void errorLexerTest(String input, @Nonnull String report) throws Exception {
		try (var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))) {
			var lexer = new Kdl2Lexer("test.kdl", inputStream, 1);
			assertThatThrownBy(() -> {
				Token token;
				do {
					token = lexer.read();
				} while (token != null);
			})
				.asInstanceOf(InstanceOfAssertFactories.type(KdlParseException.class))
				.satisfies(exception -> assertThat(Reporter.getReport(exception)).isEqualTo(report));
		}
	}

}
