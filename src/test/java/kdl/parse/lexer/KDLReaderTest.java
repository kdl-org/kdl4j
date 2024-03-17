package kdl.parse.lexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import kdl.parse.KDLInternalException;
import kdl.parse.KDLParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KDLReaderTest {

	@Test
	@DisplayName("peek() should return the next character in the stream when it is available")
	void peekOne() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("a".getBytes(StandardCharsets.UTF_8)));

		var result = reader.peek();

		assertThat(result).isEqualTo('a');
	}

	@Test
	@DisplayName("peek() should return the same character when it is called twice")
	void peekOneTwice() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("a".getBytes(StandardCharsets.UTF_8)));

		var first = reader.peek();
		var second = reader.peek();

		assertThat(first).isEqualTo(second);
	}

	@Test
	@DisplayName("peek() should return -1 when there are no more characters in the stream")
	void peekOneEOF() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

		var result = reader.peek();

		assertThat(result).isEqualTo(-1);
	}

	@Test
	@DisplayName("peek(2) should return the second character in the stream when it is available")
	void peekTwo() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("ab".getBytes(StandardCharsets.UTF_8)));

		var result = reader.peek(2);

		assertThat(result).isEqualTo('b');
	}

	@Test
	@DisplayName("peek(2) should return -1 when there is only one character available")
	void peekTwoEOF() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("a".getBytes(StandardCharsets.UTF_8)));

		var result = reader.peek(2);

		assertThat(result).isEqualTo(-1);
	}

	@Test
	@DisplayName("peek(3) should fail")
	void peekThree() {
		var reader = new KDLReader(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)));

		assertThatThrownBy(() -> reader.peek(3))
			.isInstanceOf(KDLInternalException.class)
			.hasMessage("Error while peeking: n should be between 1 and 2 included.");
	}

	@Test
	@DisplayName("read() should return the next character in the stream when it is available")
	void read() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("a".getBytes(StandardCharsets.UTF_8)));

		var result = reader.read();

		assertThat(result).isEqualTo('a');
	}

	@Test
	@DisplayName("read() should return -1 when there are no more characters in the stream")
	void readEOF() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));

		var result = reader.read();

		assertThat(result).isEqualTo(-1);
	}

	@Test
	@DisplayName("read() should return the next three characters in the stream when called thrice")
	void readThrice() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)));

		int[] result = {reader.read(), reader.read(), reader.read()};

		assertThat(result).isEqualTo(new int[]{'a', 'b', 'c'});
	}

	@Test
	@DisplayName("read() should return the peeked character when it is called after peek()")
	void readAfterPeek() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("a".getBytes(StandardCharsets.UTF_8)));

		var peeked = reader.peek();
		var read = reader.read();

		assertThat(read).isEqualTo(peeked);
	}

	@Test
	@DisplayName("read() should return the peeked characters when it is called twice after peek(2)")
	void readTwiceAfterPeekTwo() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("ab".getBytes(StandardCharsets.UTF_8)));

		reader.peek(2);
		int[] result = {reader.read(), reader.read()};

		assertThat(result).isEqualTo(new int[]{'a', 'b'});
	}

	@Test
	@DisplayName("read() should return 0xC9 when next character is É")
	void readUtf8TwoBytesCodePoint() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("É".getBytes(StandardCharsets.UTF_8)));

		var result = reader.read();

		assertThat(result).isEqualTo(0xC9);
	}

	@Test
	@DisplayName("read() should return 0x4000 when next character is 䀀")
	void readUtf8ThreeBytesCodePoint() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("䀀".getBytes(StandardCharsets.UTF_8)));

		var result = reader.read();

		assertThat(result).isEqualTo(0x4000);
	}

	@Test
	@DisplayName("read() should return 0x1F601 when next character is \uD83D\uDE01")
	void readUtf8FourBytesCodePoint() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("\uD83D\uDE01".getBytes(StandardCharsets.UTF_8)));

		var result = reader.read();

		assertThat(result).isEqualTo(0x1F601);
	}

	@Test
	@DisplayName("read() should fail when next byte is 0xF0")
	void readInvalidCodePoint() {
		var reader = new KDLReader(new ByteArrayInputStream(new byte[]{(byte) 0xF0}));

		assertThatThrownBy(reader::read)
			.isInstanceOf(KDLParseException.class)
			.hasMessage(
				"Error line 1 - Invalid character U+f0:\n" +
				"\n" +
				"▲\n" +
				"╯"
			);
	}

	@Test
	@DisplayName("peek() then read() can be repeated until the end of the stream")
	void peekThenReadThrice() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("abcd".getBytes(StandardCharsets.UTF_8)));
		var peeked = new ArrayList<Integer>();
		var read = new ArrayList<Integer>();

		for (var i = 0; i < 3; i++) {
			peeked.add(reader.peek());
			read.add(reader.read());
		}

		assertThat(read).isEqualTo(peeked);
	}

	@Test
	@DisplayName("getErrorMessage() should return a message with the current line and position when on first line")
	void testFail() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("abcdef".getBytes(StandardCharsets.UTF_8)));
		reader.read();
		reader.read();
		reader.read();

		var result = reader.error("error");
		assertThat(result).isEqualTo(
			"Error line 1 - error:\n" +
			"abcdef\n" +
			"  ▲\n" +
			"──╯"
		);
	}

	@Test
	@DisplayName("getErrorMessage() should return a message with the current line and position when on second line")
	void testFailSecondLine() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("\nabcdef".getBytes(StandardCharsets.UTF_8)));
		reader.read();
		reader.read();
		reader.read();

		var result = reader.error("error");
		assertThat(result).isEqualTo(
			"Error line 2 - error:\n" +
			"abcdef\n" +
			" ▲\n" +
			"─╯"
		);
	}

	@Test
	@DisplayName("getErrorMessage() should count CRLF as only one line")
	void testFailSecondLineWithCrLf() throws IOException {
		var reader = new KDLReader(new ByteArrayInputStream("\r\nabcdef".getBytes(StandardCharsets.UTF_8)));
		reader.read();
		reader.read();
		reader.read();

		var result = reader.error("error");
		assertThat(result).isEqualTo(
			"Error line 2 - error:\n" +
			"abcdef\n" +
			"▲\n" +
			"╯"
		);
	}

	@Test
	@DisplayName("read() should fail when the reader has been invalidated")
	void readInvalidatedReader() {
		var reader = new KDLReader(new ByteArrayInputStream("\r\nabcdef".getBytes(StandardCharsets.UTF_8)));
		reader.error("error");

		assertThatThrownBy(reader::read)
			.isInstanceOf(KDLInternalException.class)
			.hasMessage("Trying to read from an invalidated reader");
	}

	@Test
	@DisplayName("peek() should fail when the reader has been invalidated")
	void peekInvalidatedReader() {
		var reader = new KDLReader(new ByteArrayInputStream("\r\nabcdef".getBytes(StandardCharsets.UTF_8)));
		reader.error("error");

		assertThatThrownBy(reader::peek)
			.isInstanceOf(KDLInternalException.class)
			.hasMessage("Trying to peek from an invalidated reader");
	}

	@Test
	@DisplayName("peek(2) should fail when the reader has been invalidated")
	void peekTwoInvalidatedReader() {
		var reader = new KDLReader(new ByteArrayInputStream("\r\nabcdef".getBytes(StandardCharsets.UTF_8)));
		reader.error("error");

		assertThatThrownBy(() -> reader.peek(2))
			.isInstanceOf(KDLInternalException.class)
			.hasMessage("Trying to peek from an invalidated reader");
	}

	@Test
	@DisplayName("read() should fail when the next character is unicode delete")
	void unicodeDelete() {
		var reader = new KDLReader(new ByteArrayInputStream(new byte[]{0x7F}));

		assertThatThrownBy(reader::read)
			.isInstanceOf(KDLParseException.class)
			.hasMessage(
				"Error line 1 - invalid codepoint:\n" +
				"\u007f\n" +
				"▲\n" +
				"╯"
			);
	}

	@Test
	@DisplayName("read() should fail when the next character is first strong isolate")
	void firstStrongIsolate() {
		var reader = new KDLReader(new ByteArrayInputStream("\u2068".getBytes(StandardCharsets.UTF_8)));

		assertThatThrownBy(reader::read)
			.isInstanceOf(KDLParseException.class)
			.hasMessage(
				"Error line 1 - invalid codepoint:\n" +
				"\u2068\n" +
				"▲\n" +
				"╯"
			);
	}

	@Test
	@DisplayName("read() should fail when the next character is left-to-right embedding")
	void leftToRigthEmbedding() {
		var reader = new KDLReader(new ByteArrayInputStream("\u202A".getBytes(StandardCharsets.UTF_8)));

		assertThatThrownBy(reader::read)
			.isInstanceOf(KDLParseException.class)
			.hasMessage(
				"Error line 1 - invalid codepoint:\n" +
				"\u202A\n" +
				"▲\n" +
				"╯"
			);
	}

}
