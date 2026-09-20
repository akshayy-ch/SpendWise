package com.spendwise.controller;

import com.spendwise.dto.request.LoginRequest;
import com.spendwise.dto.request.RegisterRequest;
import com.spendwise.dto.request.email.ResendVerificationEmailRequest;
import com.spendwise.dto.response.LoginResponse;
import com.spendwise.dto.response.RegisterResponse;
import com.spendwise.service.AuthService;
import com.spendwise.service.EmailVerificationTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final EmailVerificationTokenService emailVerificationTokenService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request){
        log.info("Registration request received");
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        log.info("Login request received");
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {

        emailVerificationTokenService.verifyToken(token);

        return ResponseEntity.ok("Email verified successfully");
    }
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(
            @Valid @RequestBody ResendVerificationEmailRequest request) {

        authService.resendVerificationEmail(request);

        return ResponseEntity.ok("If an account exists with this email and is not yet verified, " + "a verification email has been sent.");
    }
}
