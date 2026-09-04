package com.spendwise.specification;

import com.spendwise.entity.Expense;
import com.spendwise.enums.ExpenseStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class ExpenseSpecification {

    public static Specification<Expense> hasStatus(ExpenseStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status
                );
    }

    public static Specification<Expense> hasUserId(UUID userId) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("user").get("id"),
                        userId
                );
    }

    public static Specification<Expense> hasCategory(String categoryName) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("category").get("name"),
                        categoryName
                );
    }

    public static Specification<Expense> hasWallet(String walletName){
        return (root, query, criteriaBuilder)->
                criteriaBuilder.equal(
                        root.get("wallet").get("name"),
                        walletName
                );
    }

    public static Specification<Expense> expenseAtAfter(OffsetDateTime afterDate) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.<OffsetDateTime>get("expenseAt"),
                        afterDate
                );
    }
    public static Specification<Expense> expenseAtBefore(OffsetDateTime beforeDate) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.<OffsetDateTime>get("expenseAt"),
                        beforeDate
                );
    }
}
