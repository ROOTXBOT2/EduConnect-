package com.BugJava.EduConnect.auth.exception;

/**
 * @author rua
 */
public class InvalidAccessTokenException extends RuntimeException {
    public InvalidAccessTokenException(String message) {
        super(message);
    }
}
