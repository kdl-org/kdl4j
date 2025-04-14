package dev.kdl.print;

import dev.kdl.KdlDocument;
import dev.kdl.KdlVersion;
import jakarta.annotation.Nonnull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Entry point for printing a KDL document.
 */
public class KdlPrinter {
	/**
	 * Creates a new {@link KdlPrinter} with the default configuration.
	 */
	public KdlPrinter() {
		this.configuration = KdlPrinterConfiguration.builder().build();
	}

	/**
	 * Creates a new {@link KdlPrinter} with a specific configuration.
	 *
	 * @param configuration the configuration to use when printing documents
	 */
	public KdlPrinter(KdlPrinterConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Prints a document into a string.
	 *
	 * @param document the document to print
	 * @return the printed document, in a {@link String}
	 */
	public String printToString(KdlDocument document) {
		var writer = new StringWriter();
		getContext(writer, configuration).printDocument(document);
		return writer.toString();
	}

	/**
	 * Prints a document to a {@link Writer}.
	 *
	 * @param document the document to print
	 * @param writer   the writer to write to
	 */
	public void print(KdlDocument document, Writer writer) {
		getContext(writer, configuration).printDocument(document);
	}

	/**
	 * Prints a document to an {@link OutputStream}.
	 *
	 * @param document     the document to print
	 * @param outputStream the stream to write to
	 */
	public void print(KdlDocument document, OutputStream outputStream) {
		getContext(new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)), configuration)
			.printDocument(document);
	}

	/**
	 * Prints a document to a file.
	 *
	 * @param document the document to print
	 * @param path     the path of the file to write to
	 */
	public void print(KdlDocument document, Path path) throws IOException {
		try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			getContext(writer, configuration).printDocument(document);
		}
	}

	@Nonnull
	private KdlPrinterContext getContext(Writer writer, KdlPrinterConfiguration configuration) {
		if (configuration.version() == KdlVersion.V1) {
			return new Kdl1PrinterContext(writer, configuration);
		}
		return new Kdl2PrinterContext(writer, configuration);
	}

	private final KdlPrinterConfiguration configuration;
}
