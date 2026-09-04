package com.spendwise.mapper;

import com.spendwise.dto.request.income.CreateIncomeRequest;
import com.spendwise.dto.response.income.IncomeResponse;
import com.spendwise.entity.Income;
import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IncomeMapper {

    @Mapping(target = "walletName", source = "wallet.walletName")
    IncomeResponse toResponse(Income income);

    List<IncomeResponse> toResponseList(List<Income> incomes);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "wallet", source = "wallet")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Income toEntity(
            CreateIncomeRequest request,
            User user,
            Wallet wallet
    );
}
