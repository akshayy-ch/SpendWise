package com.spendwise.mapper;

import com.spendwise.dto.request.expense.CreateExpenseRequest;
import com.spendwise.dto.response.expense.ExpenseResponse;
import com.spendwise.entity.Category;
import com.spendwise.entity.Expense;
import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "walletName", source = "wallet.walletName")
    @Mapping(target = "status", source = "status")
    ExpenseResponse toResponse(Expense expense);

    List<ExpenseResponse> toResponseList(List<Expense> expenses);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "wallet", source = "wallet")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Expense toEntity(
            CreateExpenseRequest request,
            User user,
            Wallet wallet,
            Category category,
            String status
    );
}