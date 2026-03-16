package com.g13cs3219.server.model;

import com.g13cs3219.server.exceptions.InvalidRoleException;

public enum Role {
    ADMIN,
    QUESTION_MASTER,
    USER;

    public static Role fromId(String role) {
        return switch (role) {
            case "admin" -> ADMIN;
            case "question-master" -> QUESTION_MASTER;
            case "user" -> USER;
            default -> throw new InvalidRoleException();
        };
    }
}
