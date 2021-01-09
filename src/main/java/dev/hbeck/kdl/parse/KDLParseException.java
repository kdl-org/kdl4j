package dev.hbeck.kdl.parse;

public class KDLParseException extends RuntimeException {
    public KDLParseException(String message) {
        super(message);
    }

    public KDLParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
