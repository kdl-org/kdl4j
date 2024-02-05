package kdl.parse;

/**
 * Thrown if a document cannot be parsed for any reason. The message will indicate the error and contain the line
 * and character where the parse failure occurred.
 */
public class KDLParseException extends RuntimeException {
    public KDLParseException(String message) {
        super(message);
    }

    public KDLParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
