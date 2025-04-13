package dev.kdl.parse.lexer.reader;

import dev.kdl.parse.KdlInternalParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KdlReaderTest {

	@Nested
	@DisplayName("read() should")
	class Read {

		@Test
		@DisplayName("return -1 when input stream is at end")
		void eof() throws IOException {
			var reader = reader("");

			assertThat(reader.read()).isEqualTo(KdlReader.EOF);
		}

		@Test
		@DisplayName("return 0x41 when input stream starts with 'A'")
		void oneByteCodepoint() throws IOException {
			var reader = reader("A");

			assertThat(reader.read()).isEqualTo(0x41);
		}

		@Test
		@DisplayName("return 0xB6 when input stream starts with '¶'")
		void twoBytesCodePoint() throws IOException {
			var reader = reader("¶");

			assertThat(reader.read()).isEqualTo(0xB6);
		}

		@Test
		@DisplayName("return 0x0801 when input stream starts with 'ࠁ'")
		void threeBytesCodePoint() throws IOException {
			var reader = reader("ࠁ");

			assertThat(reader.read()).isEqualTo(0x0801);
		}

		@Test
		@DisplayName("return 0x10001 when input stream starts with '\uD800\uDC01'")
		void fourBytesCodePoint() throws IOException {
			var reader = reader("\uD800\uDC01");

			assertThat(reader.read()).isEqualTo(0x10001);
		}

		@Test
		@DisplayName("throw a KdlReadException when the input stream contains an invalid 1-byte UTF-8 codepoint")
		void invalidOneByteCodepoint() {
			var reader = reader(new ByteArrayInputStream(new byte[]{-128}), 1);

			assertThatThrownBy(reader::read)
				.isInstanceOf(KdlReadException.class)
				.hasMessage("Invalid 1-byte UTF-8 codepoint: 0x80");
		}

		@Test
		@DisplayName("throw a KdlReadException when the input stream contains an invalid 2-bytes UTF-8 codepoints")
		void invalidTowBytesCodepoint() {
			var reader = reader(new ByteArrayInputStream(new byte[]{-64, 0}), 1);

			assertThatThrownBy(reader::read)
				.isInstanceOf(KdlReadException.class)
				.hasMessage("Invalid 2-bytes UTF-8 codepoint: 0xC0 0x00");
		}

		@Test
		@DisplayName("throw a KdlReadException when the input stream contains an invalid 3-bytes UTF-8 codepoints")
		void invalidThreeBytesCodepoint() {
			var reader = reader(new ByteArrayInputStream(new byte[]{-32, 0, 0}), 1);

			assertThatThrownBy(reader::read)
				.isInstanceOf(KdlReadException.class)
				.hasMessage("Invalid 3-bytes UTF-8 codepoint: 0xE0 0x00 0x00");
		}

		@Test
		@DisplayName("throw a KdlReadException when the input stream contains an invalid 4-bytes UTF-8 codepoints")
		void invalidFourBytesCodepoint() {
			var reader = reader(new ByteArrayInputStream(new byte[]{-16, 0, 0, 0}), 1);

			assertThatThrownBy(reader::read)
				.isInstanceOf(KdlReadException.class)
				.hasMessage("Invalid 4-bytes UTF-8 codepoint: 0xF0 0x00 0x00 0x00");
		}

		@Test
		@DisplayName("throw a KdlReadException when the input stream contains a UTF-8 codepoint that is invalid in a KDL document")
		void invalidKdlCodepoint() {
			var reader = reader(new ByteArrayInputStream(new byte[]{0}), 1);

			assertThatThrownBy(reader::read)
				.isInstanceOf(KdlReadException.class)
				.hasMessage("Invalid codepoint in a KDL document: U+0000");
		}

		@Test
		@DisplayName("return the peeked codepoint when peek has been called before read")
		void readAfterPeek() throws IOException {
			var reader = reader("A");
			var peeked = reader.peek();

			assertThat(reader.read()).isEqualTo(peeked);
		}

	}

	@Nested
	@DisplayName("peek() should")
	class Peek {

		@Test
		@DisplayName("return -1 when input stream is at end")
		void eof() throws IOException {
			var reader = reader("");

			assertThat(reader.peek()).isEqualTo(-1);
		}

		@Test
		@DisplayName("return 0x41 when input stream starts with 'A'")
		void singlePeek() throws IOException {
			var reader = reader("A");

			assertThat(reader.peek()).isEqualTo(0x41);
		}

		@Test
		@DisplayName("return the same result when it is called twice")
		void twoPeeks() throws IOException {
			var reader = reader("A");
			var peeked = reader.peek();

			assertThat(reader.peek()).isEqualTo(peeked);
		}

	}

	@Nested
	@DisplayName("peek(int) should")
	class PeekInt {

		@Test
		@DisplayName("return -1 when input stream is at end and n is 0")
		void peek0Eof() throws IOException {
			var reader = reader("", 2);

			assertThat(reader.peek(0)).isEqualTo(-1);
		}

		@Test
		@DisplayName("return -1 when input stream is at end and n is 1")
		void peek1Eof() throws IOException {
			var reader = reader("", 2);

			assertThat(reader.peek(1)).isEqualTo(-1);
		}

		@Test
		@DisplayName("return 0x41 when input stream starts with 'A' and n is 0")
		void peek0OneChar() throws IOException {
			var reader = reader("A", 2);

			assertThat(reader.peek(0)).isEqualTo(0x41);
		}

		@Test
		@DisplayName("return -1 when input stream contains only 'A' and n is 1")
		void peek1OneChar() throws IOException {
			var reader = reader("A", 2);

			assertThat(reader.peek(1)).isEqualTo(-1);
		}

		@Test
		@DisplayName("return the same result when it is called twice")
		void twoPeeks() throws IOException {
			var reader = reader("A", 2);
			var peeked = reader.peek(0);

			assertThat(reader.peek(0)).isEqualTo(peeked);
		}

		@Test
		@DisplayName("throw a KdlInternalException when n is negative")
		void negative() {
			var reader = reader("A", 2);

			assertThatThrownBy(() -> reader.peek(-1))
				.isInstanceOf(KdlInternalParseException.class)
				.hasMessage("Error while peeking: n should be between 0 and 1 included but was -1");
		}

		@Test
		@DisplayName("throw a KdlInternalException when n is too high for reader's capacity")
		void tooHigh() {
			var reader = reader("A", 2);

			assertThatThrownBy(() -> reader.peek(2))
				.isInstanceOf(KdlInternalParseException.class)
				.hasMessage("Error while peeking: n should be between 0 and 1 included but was 2");
		}

	}

	KdlReader reader(String string) {
		return reader(string, 1);
	}

	KdlReader reader(String string, int capacity) {
		return reader(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)), capacity);
	}

	KdlReader reader(InputStream inputStream, int capacity) {
		return new KdlReader(inputStream, capacity, (c) -> c <= 0x08);
	}
}
