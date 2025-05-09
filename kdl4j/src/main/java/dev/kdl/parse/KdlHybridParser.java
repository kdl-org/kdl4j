package dev.kdl.parse;

import dev.kdl.KdlDocument;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class KdlHybridParser implements KdlParser {
	@Nonnull
	@Override
	public KdlDocument parse(@Nullable String filename, @Nonnull InputStream inputStream) throws IOException, KdlParseException {
		try {
			return v2Parser.parse(filename, inputStream);
		} catch (KdlParseException v2Exception) {
			try {
				return v1Parser.parse(filename, inputStream);
			} catch (KdlParseException v1Exception) {
				throw new KdlHybridParseException(v1Exception, v2Exception);
			}
		}
	}

	private final Kdl1Parser v1Parser = new Kdl1Parser();
	private final Kdl2Parser v2Parser = new Kdl2Parser();
}
