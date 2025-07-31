package com.BugJava.EduConnect.auth.exception;

/**
 * @author rua
 */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
