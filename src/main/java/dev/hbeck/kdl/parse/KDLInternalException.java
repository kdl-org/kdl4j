package dev.hbeck.kdl.parse;

public class KDLInternalException extends RuntimeException {
    public KDLInternalException(String message) {
        super(message);
    }

    public KDLInternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
