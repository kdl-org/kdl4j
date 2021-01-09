package dev.hbeck.kdl.parse;

public class KDLInternalException extends KDLParseException {
    public KDLInternalException(String message) {
        super(message);
    }

    public KDLInternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
