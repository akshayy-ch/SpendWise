package com.spendwise.enums;

import lombok.Getter;

import java.util.Arrays;

public enum SettlementSortField {
    SETTLED_AT("settledAt", "settledAt"),
    AMOUNT("amount", "amount");

    private final String apiName;      // what the client is allowed to send
    @Getter
    private final String entityField;  // what actually gets passed to Sort.by()

    SettlementSortField(String apiName, String entityField) {
        this.apiName = apiName;
        this.entityField = entityField;
    }

    public static SettlementSortField fromApiName(String apiName) {
        return Arrays.stream(values())
                .filter(f -> f.apiName.equalsIgnoreCase(apiName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(apiName));
    }
}