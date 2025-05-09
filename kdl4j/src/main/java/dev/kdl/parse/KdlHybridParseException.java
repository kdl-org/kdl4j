package dev.kdl.parse;


import jakarta.annotation.Nonnull;

public class KdlHybridParseException extends KdlParseException {

	public KdlHybridParseException(@Nonnull KdlParseException v1Exception, @Nonnull KdlParseException v2Exception) {
		super("Failed to parse the document using both the KDL v2 and the KDL v1 parser.");
		this.v1Exception = v1Exception;
		this.v2Exception = v2Exception;
	}

	@Nonnull
	public KdlParseException getV1Exception() {
		return v1Exception;
	}

	@Nonnull
	public KdlParseException getV2Exception() {
		return v2Exception;
	}

	@Nonnull
	private final KdlParseException v1Exception;
	@Nonnull
	private final KdlParseException v2Exception;
}
