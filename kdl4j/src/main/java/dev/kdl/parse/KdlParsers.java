package dev.kdl.parse;


import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

final class KdlParsers {
	static KdlParser v1() {
		return KdlParsers.KDL1_PARSER.updateAndGet(parser -> parser == null ? new Kdl1Parser() : parser);
	}

	static KdlParser v2() {
		return KdlParsers.KDL2_PARSER.updateAndGet(parser -> parser == null ? new Kdl2Parser() : parser);
	}

	static KdlParser hybrid() {
		return KdlParsers.KDL_HYBRID_PARSER.updateAndGet((parser -> parser == null ? new KdlHybridParser() : parser));
	}

	static final AtomicReference<@Nullable Kdl1Parser> KDL1_PARSER = new AtomicReference<>();
	static final AtomicReference<@Nullable Kdl2Parser> KDL2_PARSER = new AtomicReference<>();
	static final AtomicReference<@Nullable KdlHybridParser> KDL_HYBRID_PARSER = new AtomicReference<>();
}
