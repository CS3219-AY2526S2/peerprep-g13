package com.g13cs3219.server.model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    private Long userId;

    private String email;
    private String role;
    private String name;
    private String username;
    private String encodedPassword;
    private String avatarUrl;
    private String preferredLanguage;
    private String preferredTopic;
    private String createdAt;
    private int createdBy;
    private Date updatedAt;
    private int updatedBy;
    private boolean isActive;
}
