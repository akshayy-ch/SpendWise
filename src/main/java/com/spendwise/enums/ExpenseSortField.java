package com.spendwise.enums;

import lombok.Getter;

import java.util.Arrays;

public enum ExpenseSortField {
    EXPENSE_AT("expenseAt", "expenseAt"),
    AMOUNT("amount", "amount");

    private final String apiName;      // what the client is allowed to send
    @Getter
    private final String entityField;  // what actually gets passed to Sort.by()

    ExpenseSortField(String apiName, String entityField) {
        this.apiName = apiName;
        this.entityField = entityField;
    }

    public static ExpenseSortField fromApiName(String apiName) {
        return Arrays.stream(values())
                .filter(f -> f.apiName.equalsIgnoreCase(apiName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(apiName));
    }

}