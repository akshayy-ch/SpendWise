package com.spendwise.controller;

import com.spendwise.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TestController {

    private final EmailService emailService;

    @PostMapping("/test-email")
    public ResponseEntity<String> testEmail() {

        emailService.sendEmail(
                "test@example.com",
                "SpendWise Email Test",
                "This is a test email from SpendWise."
        );

        return ResponseEntity.ok("Test email sent");
    }
}
