package dev.kdl.parse;

import dev.kdl.KdlDocument;
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class Kdl1Parser implements KdlParser {
	@Nonnull
	@Override
	public KdlDocument parse(@Nonnull String filename, @Nonnull InputStream inputStream) throws IOException, KdlParseException {
		return new KdlDocument(Collections.emptyList());
	}
}
