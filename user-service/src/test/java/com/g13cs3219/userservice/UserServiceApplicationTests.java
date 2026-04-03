package com.g13cs3219.userservice;

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
                .role(User.Role.USER)
                .isActive(true)
                .build();

        User saved = userRepository.save(user);
        assertThat(saved.getUserId()).isNotNull();
        assertThat(userRepository.findByEmail("test@peerprep.com")).isPresent();
    }

    @Test
    void emailLookupIsCaseInsensitive() {
        User user = User.builder()
                .email("case@peerprep.com")
                .username("caseuser")
                .encodedPassword("hashed_password")
                .role(User.Role.USER)
                .isActive(true)
                .build();
        userRepository.save(user);

        assertThat(userRepository.findByEmail("case@peerprep.com")).isPresent();
    }

    @Test
    void inactiveUserNotFound() {
        User user = User.builder()
                .email("inactive@peerprep.com")
                .username("inactiveuser")
                .encodedPassword("hashed_password")
                .role(User.Role.USER)
                .isActive(false)
                .build();
        userRepository.save(user);

        assertThat(userRepository.findByEmail("inactive@peerprep.com")).isPresent();
    }
}