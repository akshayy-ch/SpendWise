package com.spendwise.service;

import com.spendwise.dto.request.RegisterRequest;
import com.spendwise.repository.EmailVerificationTokenRepository;
import com.spendwise.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class AuthServiceEmailFailureRollbackTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void registrationEmailFailure_shouldRollbackUserAndVerificationToken() {

        String uniqueId = UUID.randomUUID().toString();

        String username = "email_failure_" + uniqueId;
        String email = "email_failure_" + uniqueId + "@test.com";
        String phoneNumber = "9" + uniqueId.replace("-", "").substring(0, 9);

        RegisterRequest request = RegisterRequest.builder()
                .username(username)
                .name("Email Failure Test")
                .email(email)
                .phoneNumber(phoneNumber)
                .password("password123")
                .build();

        doThrow(new RuntimeException("SMTP unavailable"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        assertThrows(
                RuntimeException.class,
                () -> authService.register(request)
        );

        assertEquals(
                0,
                userRepository.findByUsername(username).stream().count()
        );

        assertEquals(
                0,
                userRepository.findByEmail(email).stream().count()
        );
    }
}