package com.spendwise.service;

import com.spendwise.dto.request.expense.CreateExpenseRequest;
import com.spendwise.dto.request.expense.GetExpenseRequest;
import com.spendwise.dto.response.expense.ExpensePageResponse;
import com.spendwise.dto.response.expense.ExpenseResponse;
import com.spendwise.entity.Category;
import com.spendwise.entity.Expense;
import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import com.spendwise.enums.ExpenseSortField;
import com.spendwise.enums.ExpenseStatus;
import com.spendwise.enums.WalletStatus;
import com.spendwise.exception.CategoryExceptions.CategoryDoesNotExist;
import com.spendwise.exception.ExpenseException.InsufficientBalanceException;
import com.spendwise.exception.ExpenseException.UnauthorizedExpenseActionException;
import com.spendwise.exception.ExpenseException.VoidedExpenseException;
import com.spendwise.exception.ExpenseException.ExpenseDoesNotExist;
import com.spendwise.exception.PaginationException.InvalidPaginationException;
import com.spendwise.exception.WalletExceptions.ArchivedWalletException;
import com.spendwise.exception.WalletExceptions.InvalidAmountException;
import com.spendwise.exception.WalletExceptions.WalletDoesNotExist;
import com.spendwise.mapper.ExpenseMapper;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.ExpenseRepository;
import com.spendwise.repository.UserRepository;
import com.spendwise.repository.WalletRepository;
import com.spendwise.specification.ExpenseSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;

    private final CurrentUserService currentUserService;

    public ExpenseResponse createExpense(CreateExpenseRequest request){

        UUID userId = currentUserService.getCurrentUserId();

        Wallet wallet = walletRepository.findByWalletNameAndUserId(request.getWalletName(), userId).orElseThrow(()->new WalletDoesNotExist("Error "+request.getWalletName()));
        Category category = categoryRepository
                .findAvailableCategory(request.getCategoryName(), userId)
                .orElseThrow(() ->
                        new CategoryDoesNotExist("Category does not exist" + request.getCategoryName()));

        User userObj = userRepository.getReferenceById(userId);

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new ArchivedWalletException("Cannot create expense from an archived wallet");
        }
        if (wallet.getCurrentBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        wallet.setCurrentBalance(
                wallet.getCurrentBalance().subtract(amount)
        );
        Expense expense = expenseMapper.toEntity(request, userObj, wallet, category, request.getStatus());
        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(savedExpense);
    }

    public ExpensePageResponse getExpenses(GetExpenseRequest request, Pageable pageable){
        UUID userId = currentUserService.getCurrentUserId();
        Specification<Expense> specification = Specification.where(ExpenseSpecification.hasUserId(userId));

        if(request.getStatus() != null){
            specification = specification.and(ExpenseSpecification.hasStatus(request.getStatus()));
        }

        if(request.getCategoryName() != null){
            specification = specification.and(ExpenseSpecification.hasCategory(request.getCategoryName()));
        }
        if(request.getWalletName() != null){
            specification = specification.and(ExpenseSpecification.hasWallet(request.getWalletName()));
        }
        if(request.getFrom() != null){
            specification = specification.and(ExpenseSpecification.expenseAtAfter(request.getFrom()));
        }
        if(request.getTo() != null){
            specification = specification.and(ExpenseSpecification.expenseAtBefore(request.getTo()));
        }
        if(request.getSearch() != null){
            specification = specification.and(ExpenseSpecification.hasTitle(request.getSearch()));
        }

        Pageable customPageable = buildSafePageable(pageable);
        Page<Expense> expensePage = expenseRepository.findAll(specification, customPageable);

        Page<ExpenseResponse> responsePage = expensePage.map(expenseMapper::toResponse);

        ExpensePageResponse response = ExpensePageResponse.builder()
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

    @Transactional
    public ExpenseResponse voidExpense(UUID expenseId) {

        UUID currentUserId = currentUserService.getCurrentUserId();

        Expense expense = expenseRepository.findById(expenseId).orElseThrow(() -> new ExpenseDoesNotExist("Expense not found"));

        if (!expense.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedExpenseActionException("Only the expense creator can void the expense");
        }

        if (expense.getStatus() != ExpenseStatus.ACTIVE) {
            throw new VoidedExpenseException("Expense is already voided");
        }

        expense.setStatus(ExpenseStatus.VOIDED);

        Wallet wallet = expense.getWallet();

        wallet.setCurrentBalance(
                wallet.getCurrentBalance().add(expense.getAmount())
        );

        Expense savedExpense = expenseRepository.save(expense);


        return expenseMapper.toResponse(savedExpense);
    }

    private Pageable buildSafePageable(Pageable pageable) {
        List<Sort.Order> orders = new ArrayList<>();

        int pageSize = pageable.getPageSize();
        if (pageSize > 100) {
            throw new InvalidPaginationException("Page size not valid: " + pageSize);        }
        if (pageable.getSort().isUnsorted()) {
            orders.add(Sort.Order.desc(ExpenseSortField.EXPENSE_AT.getEntityField()));
        } else {
            for (Sort.Order order : pageable.getSort()) {
                ExpenseSortField sortField = ExpenseSortField.fromApiName(order.getProperty());
                orders.add(new Sort.Order( order.getDirection(), sortField.getEntityField()));
            }
        }

        orders.add(Sort.Order.desc("id"));
        Sort sort = Sort.by(orders);

        return PageRequest.of(pageable.getPageNumber(), pageSize, sort);
    }
}
