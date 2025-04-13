package dev.kdl.parse.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SourceLinesTest {

	@Nested
	@DisplayName("getLines(int, int) should")
	class GetLines {
		@Test
		@DisplayName("return an empty list when lines are not present")
		void missingLines() {
			var sourceLines = new SourceLines();

			var lines = sourceLines.getLines(1, 3);

			assertThat(lines).isEmpty();
		}

		@Test
		@DisplayName("return a list with lines 1 to 2 when lines they are present")
		void lines1to2() {
			var sourceLines = new SourceLines();
			addLine(sourceLines, "line 1");
			addLine(sourceLines, "line 2");

			var lines = sourceLines.getLines(1, 2);

			assertThat(lines).containsExactly(
				new SourceLine(1, "line 1"),
				new SourceLine(2, "line 2")
			);
		}

		@Test
		@DisplayName("return a list with lines 1 to 2 when lines 1 to 3 are requested but only two lines are present")
		void lines1to3WithOnly2() {
			var sourceLines = new SourceLines();
			addLine(sourceLines, "line 1");
			addLine(sourceLines, "line 2");

			var lines = sourceLines.getLines(1, 3);

			assertThat(lines).containsExactly(
				new SourceLine(1, "line 1"),
				new SourceLine(2, "line 2")
			);
		}

		@Test
		@DisplayName("return lines from 1 when startLine is 0")
		void invalidStartLine() {
			var sourceLines = new SourceLines();
			addLine(sourceLines, "line 1");
			addLine(sourceLines, "line 2");

			var lines = sourceLines.getLines(0, 2);

			assertThat(lines).containsExactly(
				new SourceLine(1, "line 1"),
				new SourceLine(2, "line 2")
			);
		}

		@Test
		@DisplayName("return an empty list when startLine is greater than endLine")
		void startLineGreaterThanEndLine() {
			var sourceLines = new SourceLines();

			var lines = sourceLines.getLines(2, 1);

			assertThat(lines).isEmpty();
		}
	}

	private void addLine(SourceLines sourceLines, String line) {
		for (var c : line.getBytes(StandardCharsets.UTF_8)) {
			sourceLines.append(c);
		}
		sourceLines.newline();
	}

}
