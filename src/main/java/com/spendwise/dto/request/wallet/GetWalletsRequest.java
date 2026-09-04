package com.spendwise.dto.request.wallet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GetWalletsRequest {

    @NotBlank(message = "Wallet name is a required field")
    @Size(min = 3, max = 100)
    private String walletName;
}
