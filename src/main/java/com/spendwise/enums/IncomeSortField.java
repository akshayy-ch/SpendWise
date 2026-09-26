package com.spendwise.enums;

import com.spendwise.exception.Income.InvalidIncomeSortFieldException;
import lombok.Getter;

import java.util.Arrays;

public enum IncomeSortField {
    INCOME_AT("incomeAt", "incomeAt"),
    AMOUNT("amount", "amount");

    private final String apiName;      // what the client is allowed to send
    @Getter
    private final String entityField;  // what actually gets passed to Sort.by()

    IncomeSortField(String apiName, String entityField) {
        this.apiName = apiName;
        this.entityField = entityField;
    }

    public static IncomeSortField fromApiName(String apiName) {
        return Arrays.stream(values())
                .filter(f -> f.apiName.equalsIgnoreCase(apiName))
                .findFirst()
                .orElseThrow(() -> new InvalidIncomeSortFieldException(apiName));
    }
}
