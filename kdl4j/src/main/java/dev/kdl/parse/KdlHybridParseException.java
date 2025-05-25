package dev.kdl.parse;


import jakarta.annotation.Nonnull;

/**
 * An exception thrown by the {@link KdlHybridParser} when both the KDL 2.0 and the KDL 1.0 parsers fail to parse the
 * document. Contains both exceptions.
 */
public class KdlHybridParseException extends KdlParseException {

	/**
	 * Creates a new {@link KdlHybridParseException}
	 *
	 * @param v1Exception the parse exception thrown by the KDL 1.0 parser
	 * @param v2Exception the parse exception thrown by the KDL 2.0 parser
	 */
	public KdlHybridParseException(@Nonnull KdlParseException v1Exception, @Nonnull KdlParseException v2Exception) {
		super("Failed to parse the document using both the KDL v2 and the KDL v1 parser.");
		this.v1Exception = v1Exception;
		this.v2Exception = v2Exception;
	}

	/**
	 * @return the exception thrown by the KDL 1.0 parser
	 */
	@Nonnull
	public KdlParseException getV1Exception() {
		return v1Exception;
	}

	/**
	 * @return the exception thrown by the KDL 2.0 parser
	 */
	@Nonnull
	public KdlParseException getV2Exception() {
		return v2Exception;
	}

	/**
	 * The exception thrown by the KDL 1.0 parser
	 */
	@Nonnull
	private final KdlParseException v1Exception;
	/**
	 * The exception thrown by the KDL 2.0 parser
	 */
	@Nonnull
	private final KdlParseException v2Exception;
}
