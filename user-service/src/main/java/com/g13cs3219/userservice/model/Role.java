package com.g13cs3219.userservice.model;

import com.g13cs3219.userservice.exceptions.InvalidRoleException;

public enum Role {
    ADMIN,
    QUESTION_MASTER,
    USER;

    public String getId() {
        return switch (this) {
            case ADMIN -> "admin";
            case QUESTION_MASTER -> "question-master";
            case USER -> "user";
        };
    }

    public static Role fromId(String role) {
        return switch (role) {
            case "admin" -> ADMIN;
            case "question-master" -> QUESTION_MASTER;
            case "user" -> USER;
            default -> throw new InvalidRoleException();
        };
    }
}