package kdl.parse;

/**
 * Thrown if an unexpected state is encountered while parsing a document. If you encounter this
 * please create an issue on https://github.com/hkolbeck/kdl4j/issues with the offending document
 */
public class KDLInternalException extends RuntimeException {
    public KDLInternalException(String message) {
        super(message);
    }

    public KDLInternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
