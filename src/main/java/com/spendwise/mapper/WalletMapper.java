package com.spendwise.mapper;

import com.spendwise.dto.request.wallet.CreateWalletRequest;
import com.spendwise.dto.response.wallet.WalletResponse;
import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import com.spendwise.enums.WalletStatus;
import com.spendwise.enums.WalletType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "type", source = "wallet.type")
    WalletResponse toResponse(Wallet wallet);

    List<WalletResponse> toResponseList(List<Wallet> wallets);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "walletName", source = "walletName")
    @Mapping(target = "type", source = "walletType")
    @Mapping(target = "currentBalance", source = "currentBalance")
    @Mapping(target = "status", source = "status")
    Wallet toEntity(
            CreateWalletRequest request,
            User user,
            String walletName,
            WalletType walletType,
            BigDecimal currentBalance,
            WalletStatus status
    );
}
