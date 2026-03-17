package com.g13cs3219.server.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.g13cs3219.server.converter.StringListConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    private String name;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, name = "encoded_password")
    private String encodedPassword;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @Convert(converter = StringListConverter.class)
    @Column(name = "preferred_topic", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> preferredTopic = new ArrayList<>();

    @Column(name = "created_by")
    private int createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Column(name = "updated_by")
    private int updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;
}
