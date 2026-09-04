package com.spendwise.mapper;

import com.spendwise.dto.response.settlement.SettlementResponse;
import com.spendwise.entity.Settlement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SettlementMapper {

    @Mapping(target = "expenseShareId", source = "expenseShare.id")
    @Mapping(target = "payerName", source = "payer.name")
    @Mapping(target = "receiverName", source = "receiver.name")
    SettlementResponse toResponse(Settlement settlement);

    List<SettlementResponse> toResponseList(
            List<Settlement> settlements
    );
}