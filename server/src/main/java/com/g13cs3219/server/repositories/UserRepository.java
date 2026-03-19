package com.g13cs3219.server.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g13cs3219.server.model.Role;
import com.g13cs3219.server.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.role = :role WHERE u = :user")
    void updateUserRole(@Param("user") User user, @Param("role") Role role);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.encodedPassword = :encodedPassword WHERE u = :user")
    void updateUserPassword(User user, String encodedPassword);
}
