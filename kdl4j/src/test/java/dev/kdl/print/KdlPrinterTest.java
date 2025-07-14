package dev.kdl.print;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import static dev.kdl.TestUtils.document;
import static dev.kdl.TestUtils.node;
import static org.assertj.core.api.Assertions.assertThat;

class KdlPrinterTest {

	@Test
	@DisplayName("printToString should return a string with the textual representation of the document")
	void printToString() throws IOException {
		var document = document(node("test"));
		var printer = KdlPrinter.v2();

		var result = printer.printToString(document);

		assertThat(result).isEqualTo("test\n");
	}

	@Test
	@DisplayName("print(Document, Writer) should write the textual representation of the document to the writer")
	void printWriter() throws IOException {
		var document = document(node("test"));
		var printer = KdlPrinter.v2();
		var writer = new StringWriter();

		printer.print(document, writer);

		assertThat(writer.toString()).isEqualTo("test\n");
	}

	@Test
	@DisplayName("print(Document, OutputStream) should write the textual representation of the document to the output stream")
	void printOutputStream() throws IOException {
		var document = document(node("test"));
		var printer = KdlPrinter.v2();
		var outputStream = new ByteArrayOutputStream();

		printer.print(document, outputStream);

		assertThat(outputStream.toString()).isEqualTo("test\n");
	}

}
