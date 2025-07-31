package com.BugJava.EduConnect.auth.exception;

/**
 * @author rua
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
