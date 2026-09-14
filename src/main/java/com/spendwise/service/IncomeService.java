package com.spendwise.service;

import com.spendwise.dto.request.income.CreateIncomeRequest;
import com.spendwise.dto.request.income.GetIncomeRequest;
import com.spendwise.dto.response.expense.ExpenseResponse;
import com.spendwise.dto.response.income.IncomePageResponse;
import com.spendwise.dto.response.income.IncomeResponse;
import com.spendwise.entity.Income;
import com.spendwise.entity.User;
import com.spendwise.entity.Wallet;
import com.spendwise.enums.ExpenseSortField;
import com.spendwise.enums.IncomeSortField;
import com.spendwise.enums.WalletStatus;
import com.spendwise.exception.PaginationException.InvalidPaginationException;
import com.spendwise.exception.WalletExceptions.ArchivedWalletException;
import com.spendwise.exception.WalletExceptions.InvalidAmountException;
import com.spendwise.exception.WalletExceptions.WalletDoesNotExist;
import com.spendwise.mapper.IncomeMapper;
import com.spendwise.repository.*;
import com.spendwise.specification.IncomeSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class IncomeService {
    private final UserRepository userRepository;
    private final IncomeRepository incomeRepository;
    private final WalletRepository walletRepository;
    private final IncomeMapper incomeMapper;

    private final CurrentUserService currentUserService;

    public  IncomeResponse createIncome(CreateIncomeRequest request){
        UUID userId = currentUserService.getCurrentUserId();
        User userObj = userRepository.getReferenceById(userId);

        Wallet wallet = walletRepository.findByWalletNameAndUserId(request.getWalletName(), userId).orElseThrow(()->new WalletDoesNotExist("Wallet not found"));
        BigDecimal amount = request.getAmount();
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new ArchivedWalletException("Cannot create income for an archived wallet");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
        wallet.setCurrentBalance(
                wallet.getCurrentBalance().add(amount)
        );

        Income income = incomeMapper.toEntity(request, userObj, wallet);

        Income createdIncome = incomeRepository.save(income);
        return incomeMapper.toResponse(createdIncome);
    }

    public IncomePageResponse getIncomes(GetIncomeRequest request, Pageable pageable){

        UUID userId = currentUserService.getCurrentUserId();
        Specification<Income> specification = Specification.where(IncomeSpecification.hasUserId(userId));

        if(request.getSource() != null){
            specification = specification.and(IncomeSpecification.hasSource(request.getSource()));
        }
        if(request.getFromDate() != null){
            specification = specification.and(IncomeSpecification.incomeAtAfter(request.getFromDate()));
        }
        if(request.getToDate() != null){
            specification = specification.and(IncomeSpecification.incomeAtBefore(request.getToDate()));
        }

        Pageable customPageable = buildSafePageable(pageable);
        Page<Income> incomePage = incomeRepository.findAll(specification, customPageable);

        Page<IncomeResponse> responsePage = incomePage.map(incomeMapper::toResponse);

        IncomePageResponse response = IncomePageResponse.builder()
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
    private Pageable buildSafePageable(Pageable pageable) {
        List<Sort.Order> orders = new ArrayList<>();

        int pageSize = pageable.getPageSize();
        if (pageSize > 100) {
            throw new InvalidPaginationException("Page size not valid: " + pageSize);        }
        if (pageable.getSort().isUnsorted()) {
            orders.add(Sort.Order.desc(IncomeSortField.INCOME_AT.getEntityField()));
        } else {
            for (Sort.Order order : pageable.getSort()) {
                IncomeSortField sortField = IncomeSortField.fromApiName(order.getProperty());
                orders.add(new Sort.Order( order.getDirection(), sortField.getEntityField()));
            }
        }

        orders.add(Sort.Order.desc("id"));
        Sort sort = Sort.by(orders);

        return PageRequest.of(pageable.getPageNumber(), pageSize, sort);
    }
}
