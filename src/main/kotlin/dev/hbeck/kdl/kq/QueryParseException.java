package dev.hbeck.kdl.kq;

public class QueryParseException extends RuntimeException {

    public QueryParseException(String message) {
        super(message);
    }

    public QueryParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
