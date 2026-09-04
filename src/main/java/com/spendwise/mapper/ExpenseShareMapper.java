package com.spendwise.mapper;

import com.spendwise.dto.response.expenseShare.ExpenseShareResponse;
import com.spendwise.entity.ExpenseShare;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExpenseShareMapper {

    @Mapping(target = "expenseId", source = "expense.id")
    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "expenseTitle", source = "expense.title")
    ExpenseShareResponse toResponse(ExpenseShare expenseShare);

    List<ExpenseShareResponse> toResponseList(
            List<ExpenseShare> expenseShares
    );
}