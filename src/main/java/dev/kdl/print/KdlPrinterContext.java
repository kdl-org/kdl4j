package dev.kdl.print;

import dev.kdl.KdlDocument;

import java.io.IOException;
import java.io.Writer;

public abstract class KdlPrinterContext {
	protected KdlPrinterContext(Writer writer, KdlPrinterConfiguration configuration) {
		this.writer = writer;
		this.configuration = configuration;
	}

	abstract void printDocument(KdlDocument document);

	protected void write(char c) {
		try {
			writer.write(c);
		} catch (IOException e) {
			throw new KdlPrintException(e);
		}
	}

	protected void write(String string) {
		try {
			writer.write(string);
		} catch (IOException e) {
			throw new KdlPrintException(e);
		}
	}

	protected int depth = 0;
	protected final Writer writer;
	protected final KdlPrinterConfiguration configuration;
}
