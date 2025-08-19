package com.BugJava.EduConnect.auth.exception;

/**
 * @author rua
 */
public class InvalidEmailPasswordException extends RuntimeException {
    public InvalidEmailPasswordException(String message) {
        super(message);
    }
}
