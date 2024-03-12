package kdl.print;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import kdl.KDLDocument;

/**
 * Entry point for printing a KDL document.
 */
public class KDLPrinter {
	/**
	 * Creates a new {@link KDLPrinter} with the default configuration.
	 */
	public KDLPrinter() {
		this.configuration = PrinterConfiguration.builder().build();
	}

	/**
	 * Creates a new {@link KDLPrinter} with a specific configuration.
	 *
	 * @param configuration the configuration to use when printing documents
	 */
	public KDLPrinter(PrinterConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Prints a document into a string.
	 *
	 * @param document the document to print
	 * @return the printed document, in a {@link String}
	 */
	public String printToString(KDLDocument document) {
		var writer = new StringWriter();
		new KDLPrinterContext(writer, configuration).printDocument(document);
		return writer.toString();
	}

	/**
	 * Prints a document to a {@link Writer}.
	 *
	 * @param document the document to print
	 * @param writer   the writer to write to
	 */
	public void print(KDLDocument document, Writer writer) {
		var context = new KDLPrinterContext(writer, configuration);
		context.printDocument(document);
	}

	/**
	 * Prints a document to an {@link OutputStream}.
	 *
	 * @param document     the document to print
	 * @param outputStream the stream to write to
	 */
	public void print(KDLDocument document, OutputStream outputStream) {
		var context = new KDLPrinterContext(new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)), configuration);
		context.printDocument(document);
	}

	/**
	 * Prints a document to a file.
	 *
	 * @param document the document to print
	 * @param path     the path of the file to write to
	 */
	public void print(KDLDocument document, Path path) throws IOException {
		try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			var context = new KDLPrinterContext(writer, configuration);
			context.printDocument(document);
		}
	}

	private final PrinterConfiguration configuration;
}
