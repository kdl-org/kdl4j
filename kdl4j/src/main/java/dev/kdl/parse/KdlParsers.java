package dev.kdl.parse;

import jakarta.annotation.Nonnull;

import java.util.concurrent.atomic.AtomicReference;

final class KdlParsers {
	@Nonnull
	static KdlParser v1() {
		return KdlParsers.KDL1_PARSER.updateAndGet(parser -> parser == null ? new Kdl1Parser() : parser);
	}

	@Nonnull
	static KdlParser v2() {
		return KdlParsers.KDL2_PARSER.updateAndGet(parser -> parser == null ? new Kdl2Parser() : parser);
	}

	@Nonnull
	static KdlParser hybrid() {
		return KdlParsers.KDL_HYBRID_PARSER.updateAndGet(parser -> parser == null ? new KdlHybridParser() : parser);
	}

	static final AtomicReference<Kdl1Parser> KDL1_PARSER = new AtomicReference<>();
	static final AtomicReference<Kdl2Parser> KDL2_PARSER = new AtomicReference<>();
	static final AtomicReference<KdlHybridParser> KDL_HYBRID_PARSER = new AtomicReference<>();
}
