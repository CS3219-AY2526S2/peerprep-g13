package com.g13cs3219.userservice.exceptions;

public class InvalidEmailFormatException extends RuntimeException {
    public InvalidEmailFormatException() {
        super("Email should be in the format of 'youremail@domain'.");
    }
}
