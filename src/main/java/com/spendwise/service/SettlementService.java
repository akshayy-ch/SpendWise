package com.spendwise.service;

import com.spendwise.dto.request.settlement.CreateSettlementRequest;
import com.spendwise.dto.response.income.IncomePageResponse;
import com.spendwise.dto.response.settlement.SettlementPageResponse;
import com.spendwise.dto.response.settlement.SettlementResponse;
import com.spendwise.entity.*;
import com.spendwise.enums.*;
import com.spendwise.exception.CategoryExceptions.CategoryDoesNotExist;
import com.spendwise.exception.ExpenseException.InsufficientBalanceException;
import com.spendwise.exception.ExpenseShareExceptions.AlreadySettledException;
import com.spendwise.exception.ExpenseShareExceptions.ExpenseShareDoesNotExist;
import com.spendwise.exception.GroupExceptions.UnauthorizedGroupActionException;
import com.spendwise.exception.PaginationException.InvalidPaginationException;
import com.spendwise.exception.SettlementExceptions.InvalidSettlementException;
import com.spendwise.exception.SettlementExceptions.ReceiverNotFound;
import com.spendwise.exception.SettlementExceptions.SettlementDoesNotExist;
import com.spendwise.exception.WalletExceptions.ArchivedWalletException;
import com.spendwise.exception.WalletExceptions.WalletDoesNotExist;
import com.spendwise.mapper.SettlementMapper;
import com.spendwise.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    private final WalletRepository walletRepository;

    @Transactional
    public SettlementResponse createSettlement(
            UUID expenseShareId,
            CreateSettlementRequest request
    ) {

        UUID currentUserId = currentUserService.getCurrentUserId();

        ExpenseShare expenseShare = expenseShareRepository.findByIdForUpdate(expenseShareId).orElseThrow(() -> new ExpenseShareDoesNotExist("Expense share not found"));

        if (!expenseShare.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedGroupActionException(
                    "Only the person who owes the share can settle it"
            );
        }

        if (expenseShare.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AlreadySettledException("This expense share is already settled");
        }

        Wallet wallet = walletRepository.findByWalletNameAndUserId(request.getWalletName(), currentUserId).orElseThrow(() -> new WalletDoesNotExist("Wallet does not exist"));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new ArchivedWalletException("Wallet is not active");
        }

        User receiver = userRepository.findById(request.getReceiverId()).orElseThrow(() -> new ReceiverNotFound("Receiver not found"));

        Category category = categoryRepository.findAvailableCategory(request.getCategoryName(), currentUserId).orElseThrow(() -> new CategoryDoesNotExist("Category does not exist: " + request.getCategoryName()));

        Expense expense = expenseShare.getExpense();

        if (!expense.getUser().getId().equals(receiver.getId())) {
            throw new InvalidSettlementException("Receiver must be the original expense payer");
        }

        BigDecimal amount = request.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidSettlementException(
                    "Settlement amount must be greater than zero"
            );
        }

        if (amount.compareTo(expenseShare.getRemainingAmount()) > 0) {
            throw new InvalidSettlementException("Settlement amount cannot exceed the remaining amount");
        }

        if (wallet.getCurrentBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        wallet.setCurrentBalance(
                wallet.getCurrentBalance().subtract(amount)
        );

        Settlement settlement = Settlement.builder()
                .amount(amount)
                .payer(expenseShare.getUser())
                .receiver(receiver)
                .expenseShare(expenseShare)
                .settledAt(OffsetDateTime.now())
                .status(SettlementStatus.SETTLED)
                .category(category)
                .wallet(wallet)
                .build();

        BigDecimal remainingAmount =
                expenseShare.getRemainingAmount().subtract(amount);

        expenseShare.setRemainingAmount(remainingAmount);

        if (remainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            expenseShare.setStatus(ExpenseShareStatus.SETTLED);
        }

        Settlement savedSettlement =
                settlementRepository.save(settlement);

        expenseShareRepository.save(expenseShare);

        walletRepository.save(wallet);

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

    public SettlementPageResponse getMySettlements(Pageable pageable) {

        UUID currentUserId = currentUserService.getCurrentUserId();

        Pageable customPageable = buildSafePageable(pageable);
        Page<Settlement> settlementPage = settlementRepository.findAllByPayerIdOrReceiverId(currentUserId, currentUserId, customPageable);
        Page<SettlementResponse> responsePage = settlementPage.map(settlementMapper::toResponse);

        SettlementPageResponse response = SettlementPageResponse.builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber())
                .size(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .first(responsePage.isFirst())
                .last(responsePage.isLast())
                .build();

        return response;
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
    private Pageable buildSafePageable(Pageable pageable) {
        List<Sort.Order> orders = new ArrayList<>();

        int pageSize = pageable.getPageSize();
        if (pageSize > 100) {
            throw new InvalidPaginationException("Page size not valid: " + pageSize);        }
        if (pageable.getSort().isUnsorted()) {
            orders.add(Sort.Order.desc(SettlementSortField.SETTLED_AT.getEntityField()));
        } else {
            for (Sort.Order order : pageable.getSort()) {
                SettlementSortField sortField = SettlementSortField.fromApiName(order.getProperty());
                orders.add(new Sort.Order( order.getDirection(), sortField.getEntityField()));
            }
        }

        orders.add(Sort.Order.desc("id"));
        Sort sort = Sort.by(orders);

        return PageRequest.of(pageable.getPageNumber(), pageSize, sort);
    }
}