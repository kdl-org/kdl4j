package dev.kdl.parse;

import dev.kdl.KdlDocument;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * A hybrid parser that first tries to parse a document using KDL 2.0 syntax, but switches to the KDL 1.0 syntax if it
 * fails. If both parsers fail, a {@link KdlHybridParseException} is thrown, containing both errors.
 */
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
