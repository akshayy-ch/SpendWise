package com.spendwise.dto.request.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ResendVerificationEmailRequest {
    @NotBlank(message = "Email is a required field")
    @Email(message = "Invalid email format")
    private String email;
}