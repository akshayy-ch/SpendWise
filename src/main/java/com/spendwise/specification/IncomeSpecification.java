package com.spendwise.specification;

import com.spendwise.entity.Expense;
import com.spendwise.entity.Income;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.UUID;

public class IncomeSpecification {

    public static Specification<Income> hasUserId(UUID userId) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("user").get("id"),
                        userId
                );
    }

    public static Specification<Income> hasSource(String source){
        return (root, query, criteriaBuilder)->
                criteriaBuilder.equal(
                        root.get("source"),
                        source
                );
    }
    public static Specification<Income> incomeAtAfter(OffsetDateTime afterDate) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.<OffsetDateTime>get("incomeAt"),
                        afterDate
                );
    }
    public static Specification<Income> incomeAtBefore(OffsetDateTime beforeDate) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.<OffsetDateTime>get("incomeAt"),
                        beforeDate
                );
    }

}
