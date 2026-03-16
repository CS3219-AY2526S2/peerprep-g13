package com.g13cs3219.server.exceptions;

public class InvalidUserIDException extends RuntimeException {
    public InvalidUserIDException(String fieldName) {
        super("Invalid " + fieldName + " ID, " +
                fieldName + " ID should be a positive integer and less than the total number of users.");
    }
}
