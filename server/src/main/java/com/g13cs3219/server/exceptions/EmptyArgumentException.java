package com.g13cs3219.server.exceptions;

public class EmptyArgumentException extends RuntimeException {
    public EmptyArgumentException() {
        super("Email and password cannot be empty.");
    }
}
