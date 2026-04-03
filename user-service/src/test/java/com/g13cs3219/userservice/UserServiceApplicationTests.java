package com.g13cs3219.userservice;

import com.g13cs3219.userservice.model.Role;
import com.g13cs3219.userservice.model.User;
import com.g13cs3219.userservice.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void canSaveAndFindUser() {
        User user = User.builder()
                .email("test@peerprep.com")
                .username("testuser")
                .encodedPassword("hashed_password")
                .role(Role.USER)
                .build();

        User saved = userRepository.save(user);
        assertThat(saved.getUserId()).isNotNull();
        assertThat(userRepository.findByEmail("test@peerprep.com")).isPresent();
    }

    @Test
    void duplicateEmailDetected() {
        User user = User.builder()
                .email("duplicate@peerprep.com")
                .username("dupuser")
                .encodedPassword("hashed_password")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        assertThat(userRepository.findByEmail("duplicate@peerprep.com")).isPresent();
    }

    @Test
    void adminRoleSavedCorrectly() {
        User admin = User.builder()
                .email("admin@peerprep.com")
                .username("adminuser")
                .encodedPassword("hashed_password")
                .role(Role.ADMIN)
                .build();

        User saved = userRepository.save(admin);
        assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
    }
}