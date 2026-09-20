package com.spendwise.service;

import com.spendwise.entity.EmailVerificationToken;
import com.spendwise.entity.User;
import com.spendwise.exception.EmailExceptions.EmailVerificationTokenAlreadyUsedException;
import com.spendwise.exception.EmailExceptions.EmailVerificationTokenExpiredException;
import com.spendwise.repository.EmailVerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailVerificationTokenService {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Transactional
    public String createVerificationToken(User user) {

        List<EmailVerificationToken> existingTokens = emailVerificationTokenRepository.findByUserIdAndUsedFalse(user.getId());

        for (EmailVerificationToken token : existingTokens) {
            token.setUsed(true);
        }

        String rawToken = generateToken();
        String tokenHash = hashToken(rawToken);

        EmailVerificationToken verificationToken =
                EmailVerificationToken.builder()
                        .tokenHash(tokenHash)
                        .user(user)
                        .expiresAt(OffsetDateTime.now().plusMinutes(30))
                        .used(false)
                        .build();

        emailVerificationTokenRepository.save(verificationToken);
        return rawToken;
    }

    @Transactional
    public void verifyToken(String rawToken) {

        String tokenHash = hashToken(rawToken);

        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByTokenHash(tokenHash).orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new EmailVerificationTokenAlreadyUsedException("Verification token has already been used");
        }

        if (verificationToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new EmailVerificationTokenExpiredException("Verification token has expired");
        }

        User user = verificationToken.getUser();

        user.setEmailVerified(true);

        verificationToken.setUsed(true);
    }

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}