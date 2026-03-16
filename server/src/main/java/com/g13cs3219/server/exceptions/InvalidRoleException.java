package com.g13cs3219.server.exceptions;

public class InvalidRoleException extends RuntimeException {
    public InvalidRoleException() {
        super("Role should be one of 'admin', 'question-master', 'user'.");
    }
}
