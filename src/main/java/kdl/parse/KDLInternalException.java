package kdl.parse;

/**
 * Thrown if an unexpected state is encountered while parsing a document. If you encounter this
 * please report an issue with the offending document.
 */
public class KDLInternalException extends RuntimeException {
	public KDLInternalException(String message) {
		super(message);
	}

}
