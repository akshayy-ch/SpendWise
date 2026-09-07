package com.spendwise.service;

import com.spendwise.dto.request.settlement.CreateSettlementRequest;
import com.spendwise.dto.response.settlement.SettlementResponse;
import com.spendwise.entity.*;
import com.spendwise.enums.ExpenseShareStatus;
import com.spendwise.enums.SettlementStatus;
import com.spendwise.exception.CategoryExceptions.CategoryDoesNotExist;
import com.spendwise.exception.ExpenseShareExceptions.AlreadySettledException;
import com.spendwise.exception.ExpenseShareExceptions.ExpenseShareDoesNotExist;
import com.spendwise.exception.GroupExceptions.UnauthorizedGroupActionException;
import com.spendwise.exception.SettlementExceptions.InvalidSettlementException;
import com.spendwise.exception.SettlementExceptions.ReceiverNotFound;
import com.spendwise.exception.SettlementExceptions.SettlementDoesNotExist;
import com.spendwise.mapper.SettlementMapper;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.ExpenseShareRepository;
import com.spendwise.repository.SettlementRepository;
import com.spendwise.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final ExpenseShareRepository expenseShareRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final SettlementMapper settlementMapper;
    private final CategoryRepository categoryRepository;

    @Transactional
    public SettlementResponse createSettlement(UUID expenseShareId, CreateSettlementRequest request) {

        UUID currentUserId = currentUserService.getCurrentUserId();
        ExpenseShare expenseShare =expenseShareRepository.findById(expenseShareId).orElseThrow(() ->new ExpenseShareDoesNotExist("Expense share not found"));
        if (!expenseShare.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedGroupActionException("Only the person who owes the share can settle it");
        }
        if (expenseShare.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AlreadySettledException("This expense share is already settled");
        }

        User receiver = userRepository.findById(request.getReceiverId()).orElseThrow(() -> new ReceiverNotFound("Receiver not found"));

        Category category = categoryRepository
                .findAvailableCategory(request.getCategoryName(), currentUserId)
                .orElseThrow(() ->
                        new CategoryDoesNotExist("Category does not exist"));
        Expense expense = expenseShare.getExpense();
        if (!expense.getUser().getId().equals(receiver.getId())) {
            throw new InvalidSettlementException("Receiver must be the original expense payer");
        }
        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidSettlementException("Settlement amount must be greater than zero");
        }

        if (amount.compareTo(expenseShare.getRemainingAmount()) > 0) {
            throw new InvalidSettlementException("Settlement amount cannot exceed the remaining amount");
        }

        Settlement settlement = Settlement.builder()
                .amount(amount)
                .payer(expenseShare.getUser())
                .receiver(receiver)
                .expenseShare(expenseShare)
                .settledAt(OffsetDateTime.now())
                .status(SettlementStatus.SETTLED)
                .category(category)
                .build();

        BigDecimal remainingAmount = expenseShare.getRemainingAmount().subtract(amount);

        expenseShare.setRemainingAmount(remainingAmount);

        if (remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            expenseShare.setStatus(ExpenseShareStatus.SETTLED);
        }

        Settlement savedSettlement = settlementRepository.save(settlement);

        expenseShareRepository.save(expenseShare);

        return settlementMapper.toResponse(savedSettlement);
    }

    public SettlementResponse getSettlement(UUID settlementId) {

        UUID currentUserId = currentUserService.getCurrentUserId();

        Settlement settlement = settlementRepository.findById(settlementId).orElseThrow(() -> new SettlementDoesNotExist("Settlement not found"));

        if (!settlement.getPayer().getId().equals(currentUserId) && !settlement.getReceiver().getId().equals(currentUserId)) {
            throw new UnauthorizedGroupActionException("You are not authorized to view this settlement");
        }
        return settlementMapper.toResponse(settlement);
    }

    public List<SettlementResponse> getMySettlements() {

        UUID currentUserId = currentUserService.getCurrentUserId();

        List<Settlement> settlements = settlementRepository.findAllByPayerIdOrReceiverId(currentUserId, currentUserId);

        return settlementMapper.toResponseList(settlements);
    }

    public List<SettlementResponse> getSettlementsForShare(UUID expenseShareId) {

        UUID currentUserId = currentUserService.getCurrentUserId();

        ExpenseShare expenseShare = expenseShareRepository.findById(expenseShareId).orElseThrow(() -> new ExpenseShareDoesNotExist("Expense share not found"));
        boolean isPayer = expenseShare.getUser().getId().equals(currentUserId);

        boolean isReceiver = expenseShare.getExpense().getUser().getId().equals(currentUserId);

        if (!isPayer && !isReceiver) {
            throw new UnauthorizedGroupActionException("You are not authorized to view these settlements");
        }

        List<Settlement> settlements = settlementRepository.findAllByExpenseShareId(expenseShareId);

        return settlementMapper.toResponseList(settlements);
    }
}