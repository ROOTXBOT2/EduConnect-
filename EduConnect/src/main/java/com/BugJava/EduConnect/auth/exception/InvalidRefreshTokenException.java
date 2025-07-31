package com.BugJava.EduConnect.auth.exception;

/**
 * @author rua
 */
public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
