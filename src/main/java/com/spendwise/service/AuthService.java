package com.spendwise.service;

import com.spendwise.dto.request.LoginRequest;
import com.spendwise.dto.request.RegisterRequest;
import com.spendwise.dto.response.LoginResponse;
import com.spendwise.dto.response.RegisterResponse;
import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import com.spendwise.enums.WalletStatus;
import com.spendwise.enums.WalletType;
import com.spendwise.exception.AuthExceptions.EmailAlreadyExistsException;
import com.spendwise.exception.AuthExceptions.PhoneNoAlreadyExistsException;
import com.spendwise.exception.AuthExceptions.UsernameAlreadyExistsException;
import com.spendwise.repository.UserRepository;
import com.spendwise.repository.WalletRepository;
import com.spendwise.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final WalletRepository walletRepository;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request){
        log.info("Registering user with username={}", request.getUsername());
        if(userRepository.existsByUsername(request.getUsername())){
            throw new UsernameAlreadyExistsException("UserName already in use");
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException("Email already in use");
        }
        if(userRepository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new PhoneNoAlreadyExistsException("Phone No. already in use");
        }
        User createdUser = User.builder()
                .username(request.getUsername())
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(createdUser);
        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .walletName("Cash")
                .currentBalance(BigDecimal.valueOf(10000))
                .status(WalletStatus.ACTIVE)
                .type(WalletType.CASH)
                .build();

        walletRepository.save(wallet);

        log.info("User registered successfully with id={}", savedUser.getId());
        return RegisterResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .message("User registered successfully")
                .walletName(wallet.getWalletName())
                .amount(wallet.getCurrentBalance())
                .build();
    }
    public LoginResponse login(LoginRequest request){
        log.info("Login request with username={}", request.getUsername());
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword());
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        String token = jwtService.generateToken(authentication.getName());
        return LoginResponse.builder()
                .authToken(token)
                .build();
    }
}

