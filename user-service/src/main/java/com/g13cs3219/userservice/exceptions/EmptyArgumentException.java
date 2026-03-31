package com.g13cs3219.userservice.exceptions;

public class EmptyArgumentException extends RuntimeException {
    public EmptyArgumentException() {
        super("Email and password cannot be empty.");
    }
}
