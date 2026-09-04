package com.spendwise.dto.request.income;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GetIncomeRequest {

    @Size(min = 3, max = 50)
    private String source;

    private OffsetDateTime fromDate;

    private OffsetDateTime toDate;
}
