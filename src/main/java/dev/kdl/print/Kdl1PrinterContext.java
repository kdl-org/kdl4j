package dev.kdl.print;

import dev.kdl.KdlDocument;

import java.io.Writer;

public class Kdl1PrinterContext extends KdlPrinterContext {
	Kdl1PrinterContext(Writer writer, KdlPrinterConfiguration configuration) {
		super(writer, configuration);
	}

	@Override
	void printDocument(KdlDocument document) {
		// TODO
	}
}
