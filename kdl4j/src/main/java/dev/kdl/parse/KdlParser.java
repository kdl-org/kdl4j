package dev.kdl.parse;

import dev.kdl.KdlDocument;
import dev.kdl.KdlVersion;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public interface KdlParser {

	/**
	 * Parses an input stream as a {@link  KdlDocument}.
	 *
	 * @param filename    the name of the parsed file
	 * @param inputStream an input stream returning a KDL document
	 * @return a {@link KdlDocument} corresponding to {@code document}
	 * @throws IOException       if an error occurs while reading the input
	 * @throws KdlParseException if the document is invalid
	 */
	@Nonnull
	KdlDocument parse(@Nullable String filename, @Nonnull InputStream inputStream) throws IOException, KdlParseException;

	/**
	 * Parses an input stream as a {@link  KdlDocument}.
	 *
	 * @param inputStream an input stream returning a KDL document
	 * @return a {@link KdlDocument} corresponding to {@code document}
	 * @throws IOException       if an error occurs while reading the input
	 * @throws KdlParseException if the document is invalid
	 */
	@Nonnull
	default KdlDocument parse(@Nonnull InputStream inputStream) throws IOException, KdlParseException {
		return parse(null, inputStream);
	}

	/**
	 * Parses a file as a {@link  KdlDocument}.
	 *
	 * @param path path to a file containing a KDL document
	 * @return a {@link KdlDocument} corresponding to {@code document}
	 * @throws IOException       if an error occurs while reading the input
	 * @throws KdlParseException if the document is invalid
	 */
	@Nonnull
	default KdlDocument parse(@Nonnull Path path) throws IOException, KdlParseException {
		try (var fileInputStream = Files.newInputStream(path)) {
			return parse(path.toString(), new BufferedInputStream(fileInputStream));
		}
	}

	/**
	 * Parses a string as a {@link  KdlDocument}.
	 *
	 * @param filename the name of the parsed file
	 * @param document a string representation of a KDL document
	 * @return a {@link KdlDocument} corresponding to {@code document}
	 * @throws IOException       if an error occurs while reading the input
	 * @throws KdlParseException if the document is invalid
	 */
	@Nonnull
	default KdlDocument parse(@Nullable String filename, @Nonnull String document) throws IOException, KdlParseException {
		return parse(filename, new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * Parses a string as a {@link  KdlDocument}.
	 *
	 * @param document a string representation of a KDL document
	 * @return a {@link KdlDocument} corresponding to {@code document}
	 * @throws IOException       if an error occurs while reading the input
	 * @throws KdlParseException if the document is invalid
	 */
	@Nonnull
	default KdlDocument parse(@Nonnull String document) throws IOException, KdlParseException {
		return parse(null, document);
	}

	/**
	 * Creates a new KDL 1 parser.
	 *
	 * @return a KDL 1 parser
	 */
	@Nonnull
	static KdlParser v1() {
		return KDL1_PARSER.updateAndGet(parser -> parser == null ? new Kdl1Parser() : parser);
	}

	/**
	 * Creates a new KDL 2 parser.
	 *
	 * @return a KDL 2 parser
	 */
	@Nonnull
	static KdlParser v2() {
		return KDL2_PARSER.updateAndGet(parser -> parser == null ? new Kdl2Parser() : parser);
	}

	/**
	 * Creates a new parser in hybrid mode: it first tries to parse the document as a KDL 2 document, then tries to
	 * parse it as a KDL 1 document if it fails.
	 *
	 * @return a hybrid KDL parser
	 */
	@Nonnull
	static KdlParser hybrid() {
		return KDL_HYBRID_PARSER.updateAndGet(parser -> parser == null ? new KdlHybridParser() : parser);
	}

	AtomicReference<Kdl1Parser> KDL1_PARSER = new AtomicReference<>();
	AtomicReference<Kdl2Parser> KDL2_PARSER = new AtomicReference<>();
	AtomicReference<KdlHybridParser> KDL_HYBRID_PARSER = new AtomicReference<>();

	/**
	 * Creates a new parser depending on the specified version. If no version is specified, creates a hybrid parser.
	 *
	 * @param version the version of the created parser
	 * @return a new parser
	 */
	@Nonnull
	static KdlParser fromVersion(@Nullable KdlVersion version) {
		if (version == null) {
			return hybrid();
		}
		return switch (version) {
			case V1 -> v1();
			case V2 -> v2();
		};
	}
}
