package dev.kdl.parse;

import dev.kdl.parse.context.ParseContext;
import dev.kdl.parse.context.SourceLine;
import dev.kdl.parse.context.Span;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ReporterTest {

	@ParameterizedTest
	@MethodSource("testCases")
	void reportTest(KdlParseException exception, String expected) {
		var result = Reporter.getReport(exception);
		assertThat(result).isEqualTo(expected);
	}

	static Stream<Arguments> testCases() {
		return Stream.of(
			Arguments.of(new KdlParseException("Something went wrong"), "× Something went wrong"),
			Arguments.of(
				new KdlParseException("Something went wrong", "you should consider fixing this"),
				"× Something went wrong\n" +
					"help: you should consider fixing this"
			),
			Arguments.of(
				new KdlParseException(
					"Something went wrong",
					new ParseContext(
						null,
						List.of(new SourceLine(1, "first and only line")),
						Span.of(1, 7, 1, 9)
					)
				),
				"""
					× Something went wrong:
					  ╭─[1:7]
					1 │ first and only line
					  ╰─"""
			),
			Arguments.of(
				new KdlParseException(
					"Something went wrong",
					new ParseContext(
						"test.kdl",
						List.of(new SourceLine(1, "first and only line")),
						Span.of(1, 7, 1, 9)
					)
				),
				"""
					× Something went wrong:
					  ╭─[test.kdl:1:7]
					1 │ first and only line
					  ╰─"""
			),
			Arguments.of(
				new KdlParseException(
					"Something went wrong",
					new ParseContext(
						"test.kdl",
						List.of(new SourceLine(1, "first and only line")),
						Span.of(1, 7, 1, 9)
					),
					"this is a very long label"
				),
				"""
					× Something went wrong:
					  ╭─[test.kdl:1:7]
					1 │ first and only line
					  ·       ─┬─
					  ·        ╰ this is a very long label
					  ╰─"""
			),
			Arguments.of(
				new KdlParseException(
					"Something went wrong",
					new ParseContext(
						"test.kdl",
						List.of(new SourceLine(1, "first and only line")),
						Span.of(1, 7, 1, 9)
					),
					"short"
				),
				"""
					× Something went wrong:
					  ╭─[test.kdl:1:7]
					1 │ first and only line
					  ·       ─┬─
					  ·  short ╯
					  ╰─"""
			),
			Arguments.of(
				new KdlParseException(
					"Something went wrong",
					new ParseContext(
						"test.kdl",
						List.of(new SourceLine(1, "first and only line")),
						Span.of(1, 7, 1, 9)
					),
					"short",
					"you should consider fixing this"
				),
				"""
					× Something went wrong:
					  ╭─[test.kdl:1:7]
					1 │ first and only line
					  ·       ─┬─
					  ·  short ╯
					  ╰─
					help: you should consider fixing this"""
			),
			Arguments.of(
				new KdlParseException(
					"Something went wrong",
					new ParseContext(
						"test.kdl",
						List.of(
							new SourceLine(33, "line thirty-three"),
							new SourceLine(34, "line thirty-four"),
							new SourceLine(35, "last line (thirty-five)")
						),
						Span.of(33, 6, 35, 9)
					),
					"this is a very long label"
				),
				"""
					× Something went wrong:
					   ╭─[test.kdl:33:6]
					33 │ line thirty-three
					   ·      ────────────
					34 │ line thirty-four
					   · ────────────────
					35 │ last line (thirty-five)
					   · ────┬────
					   ·     ╰ this is a very long label
					   ╰─"""
			),
			Arguments.of(
				new KdlParseException(
					"Something went wrong",
					new ParseContext(
						"test.kdl",
						List.of(
							new SourceLine(33, "line thirty-three"),
							new SourceLine(34, "line thirty-four"),
							new SourceLine(35, "last line (thirty-five)")
						),
						Span.of(33, 13, 35, 9)
					),
					"this is a very long label"
				),
				"""
					× Something went wrong:
					   ╭─[test.kdl:33:13]
					33 │ line thirty-three
					   ·             ─────
					34 │ line thirty-four
					   · ────────────────
					35 │ last line (thirty-five)
					   · ────┬────
					   ·     ╰ this is a very long label
					   ╰─"""
			),
			Arguments.of(
				new KdlParseException(
					"Something went wrong",
					new ParseContext(
						"test.kdl",
						List.of(
							new SourceLine(33, "line thirty-three"),
							new SourceLine(34, "line thirty-four"),
							new SourceLine(35, "last line (thirty-five)")
						),
						Span.of(33, 6, 34, 1)
					),
					"this is a very long label"
				),
				"""
					× Something went wrong:
					   ╭─[test.kdl:33:6]
					33 │ line thirty-three
					   ·      ────────────
					34 │ line thirty-four
					   · ┬
					   · ╰ this is a very long label
					35 │ last line (thirty-five)
					   ╰─"""
			),
			Arguments.of(
				new KdlParseException(
					"Something went wrong",
					new ParseContext(
						"test.kdl",
						List.of(new SourceLine(737, "line seven hundred and thirty-seven")),
						Span.of(737, 35, 737, 35)
					),
					"this is a very long label"
				),
				"""
					× Something went wrong:
					    ╭─[test.kdl:737:35]
					737 │ line seven hundred and thirty-seven
					    ·                                   ┬
					    ·         this is a very long label ╯
					    ╰─"""
			),
			Arguments.of(
				new KdlHybridParseException(
					new KdlParseException(
						"this error happened in the KDL v2 parser",
						new ParseContext(
							"test.kdl",
							List.of(new SourceLine(737, "line seven hundred and thirty-seven")),
							Span.of(737, 6, 737, 10)
						),
						"oops",
						"maybe try with the KDL v1 parser"
					),
					new KdlParseException(
						"this error happened in the KDL v1 parser",
						new ParseContext(
							"test.kdl",
							List.of(new SourceLine(33, "line thirty-three")),
							Span.of(33, 6, 33, 11)
						),
						"oops",
						"this is hopeless"
					)
				),
				"""
					Failed to parse the document using both the KDL v2 and the KDL v1 parser.
					KDL v2 error:
					× this error happened in the KDL v2 parser:
					    ╭─[test.kdl:737:6]
					737 │ line seven hundred and thirty-seven
					    ·      ──┬──
					    ·   oops ╯
					    ╰─
					help: maybe try with the KDL v1 parser
					KDL v1 error:
					× this error happened in the KDL v1 parser:
					   ╭─[test.kdl:33:6]
					33 │ line thirty-three
					   ·      ──┬───
					   ·   oops ╯
					   ╰─
					help: this is hopeless"""
			)
		);
	}

}
