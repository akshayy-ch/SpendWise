package com.spendwise.service;

import com.spendwise.dto.request.budget.UpdateBudgetRequest;
import com.spendwise.dto.request.budget.createBudgetRequest;
import com.spendwise.dto.response.budget.BudgetResponse;
import com.spendwise.entity.Budget;
import com.spendwise.entity.BudgetPeriod;
import com.spendwise.entity.Category;
import com.spendwise.entity.User;
import com.spendwise.exception.BudgetExceptions.*;
import com.spendwise.exception.CategoryExceptions.CategoryDoesNotExist;
import com.spendwise.repository.BudgetPeriodRepository;
import com.spendwise.repository.BudgetRepository;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetServices {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;
    private final BudgetRepository budgetRepository;
    private final BudgetPeriodRepository budgetPeriodRepository;

    @Transactional
    public BudgetResponse createBudget(createBudgetRequest request){
        UUID userId = currentUserService.getCurrentUserId();

        User userObj = userRepository.getReferenceById(userId);


        if(budgetPeriodRepository.existsByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userId, LocalDate.now(), LocalDate.now())){
            throw new BudgetAlreadyExistsException("Active budget already exists");
        }
        if(!checkDateValidity(request.getStartDate(), request.getEndDate())){
            throw new InvalidStartandEndDateException("Invalid start and end date");
        }
        BudgetPeriod budgetPeriod = BudgetPeriod.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .user(userObj)
                .build();

        BudgetPeriod savedBudgetPeriod = budgetPeriodRepository.save(budgetPeriod);
        BigDecimal overAllBudgetLimit = request.getOverallBudget();
        BigDecimal totalCategoryBudget = BigDecimal.ZERO;

        List<Budget> categoryBudgetList = new ArrayList<>();

        Set<String> categoryNames = new HashSet<>();

        for (createBudgetRequest.CategoryBudgetItem item : request.getCategoryBudgets()) {
            String categoryName = item.getCategoryName().trim().toLowerCase();            BigDecimal limit = item.getLimit();

            if (!categoryNames.add(categoryName)) {
                throw new DuplicateBudgetCategoryException(
                        "Category cannot be added more than once"
                );
            }
            Category category = categoryRepository
                    .findAvailableCategory(categoryName, userId)
                    .orElseThrow(() -> new CategoryDoesNotExist("Category does not exist"));
            totalCategoryBudget = totalCategoryBudget.add(limit);

            Budget createdBudget = Budget.builder()
                    .budgetLimit(limit)
                    .category(category)
                    .user(userObj)
                    .budgetPeriod(budgetPeriod)
                    .build();
            categoryBudgetList.add(createdBudget);
        }

        if(overAllBudgetLimit != null && totalCategoryBudget.compareTo(overAllBudgetLimit) > 0){
            throw new InvalidBudgetLimitException("Total category budget exceeds overall budget");
        }

        budgetRepository.saveAll(categoryBudgetList);
        Budget overallBudget = null;
        if(overAllBudgetLimit != null){
            overallBudget = Budget.builder()
                    .budgetLimit(overAllBudgetLimit)
                    .user(userObj)
                    .budgetPeriod(savedBudgetPeriod)
                    .build();

            budgetRepository.save(overallBudget);
        }

        List<BudgetResponse.CategoryBudgetItem> categoryResponses =
                categoryBudgetList.stream()
                        .map(budget -> BudgetResponse.CategoryBudgetItem.builder()
                                .categoryName(budget.getCategory().getName())
                                .limit(budget.getBudgetLimit())
                                .build())
                        .toList();
        return BudgetResponse.builder()
                .id(savedBudgetPeriod.getId())
                .overallBudget(overallBudget != null ? overallBudget.getBudgetLimit() : null)
                .categoryBudgets(categoryResponses)
                .startDate(savedBudgetPeriod.getStartDate())
                .endDate(savedBudgetPeriod.getEndDate())
                .build();
    }

    public BudgetResponse getCurrentBudget(){
        UUID userId = currentUserService.getCurrentUserId();

        BudgetPeriod budgetPeriod = budgetPeriodRepository
                .findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userId, LocalDate.now(), LocalDate.now())
                .orElseThrow(() -> new BudgetNotFoundException("No budget exists currently"));

        List<Budget> budgetList = budgetPeriod.getBudgets();

        Budget overallBudget = budgetList.stream()
                .filter(budget -> budget.getCategory() == null)
                .findFirst()
                .orElse(null);

        List<BudgetResponse.CategoryBudgetItem> categoryBudgets =
                budgetList.stream()
                        .filter(budget -> budget.getCategory() != null)
                        .map(budget -> BudgetResponse.CategoryBudgetItem.builder()
                                .categoryName(budget.getCategory().getName())
                                .limit(budget.getBudgetLimit())
                                .build())
                        .toList();
        return BudgetResponse.builder()
                .id(budgetPeriod.getId())
                .overallBudget( overallBudget != null ? overallBudget.getBudgetLimit() : null)
                .categoryBudgets(categoryBudgets)
                .startDate(budgetPeriod.getStartDate())
                .endDate(budgetPeriod.getEndDate())
                .build();
    }

    @Transactional
    public BudgetResponse updateBudget(UpdateBudgetRequest request) {

        UUID userId = currentUserService.getCurrentUserId();

        BudgetPeriod budgetPeriod = budgetPeriodRepository
                .findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        userId,
                        LocalDate.now(),
                        LocalDate.now()
                )
                .orElseThrow(() ->
                        new BudgetNotFoundException("No budget exists currently"));

        List<Budget> budgetList = budgetPeriod.getBudgets();

        //Current overall budget

        Budget currentOverallBudget = budgetList.stream()
                .filter(budget -> budget.getCategory() == null)
                .findFirst()
                .orElse(null);

        BigDecimal finalOverallBudget =
                request.getOverallBudget() != null
                        ? request.getOverallBudget()
                        : currentOverallBudget != null
                        ? currentOverallBudget.getBudgetLimit()
                        : null;

        //Update overall budget

        if (request.getOverallBudget() != null) {

            if (currentOverallBudget != null) {

                currentOverallBudget.setBudgetLimit(
                        request.getOverallBudget()
                );

            } else {

                Budget newOverallBudget = Budget.builder()
                        .budgetLimit(request.getOverallBudget())
                        .user(budgetPeriod.getUser())
                        .budgetPeriod(budgetPeriod)
                        .build();

                budgetList.add(newOverallBudget);
            }
        }

        //Update categories

        if (request.getCategoryBudgets() != null) {

            Set<String> categoryNames = new HashSet<>();

            for (UpdateBudgetRequest.CategoryBudgetItem item
                    : request.getCategoryBudgets()) {

                String categoryName = item.getCategoryName().trim().toLowerCase();

                if (!categoryNames.add(categoryName)) {
                    throw new DuplicateBudgetCategoryException(
                            "Category cannot be added more than once"
                    );
                }
                Category category = categoryRepository
                        .findAvailableCategory(
                                item.getCategoryName(),
                                userId
                        )
                        .orElseThrow(() ->
                                new CategoryDoesNotExist(
                                        "Category does not exist"
                                ));

                // Check whether this category already has a budget
                Budget existingBudget = budgetList.stream()
                        .filter(budget ->
                                budget.getCategory() != null &&
                                        budget.getCategory().getId()
                                                .equals(category.getId())
                        )
                        .findFirst()
                        .orElse(null);

                if (existingBudget != null) {
                    existingBudget.setBudgetLimit(item.getLimit());
                } else {
                    Budget newCategoryBudget = Budget.builder()
                            .budgetLimit(item.getLimit())
                            .category(category)
                            .user(budgetPeriod.getUser())
                            .budgetPeriod(budgetPeriod)
                            .build();

                    budgetList.add(newCategoryBudget);
                }
            }
        }

        BigDecimal finalCategoryTotal = budgetList.stream()
                .filter(budget -> budget.getCategory() != null)
                .map(Budget::getBudgetLimit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (finalOverallBudget != null &&
                finalCategoryTotal.compareTo(finalOverallBudget) > 0) {

            throw new InvalidBudgetLimitException(
                    "Total category budget exceeds overall budget"
            );
        }

        // Update dates

        LocalDate finalStartDate =
                request.getStartDate() != null
                        ? request.getStartDate()
                        : budgetPeriod.getStartDate();

        LocalDate finalEndDate =
                request.getEndDate() != null
                        ? request.getEndDate()
                        : budgetPeriod.getEndDate();

        if (!checkUpdatedDateValidity(finalStartDate, finalEndDate)) {
            throw new InvalidStartandEndDateException(
                    "Invalid start and end date"
            );
        }

        budgetPeriod.setStartDate(finalStartDate);
        budgetPeriod.setEndDate(finalEndDate);

        //Save

        budgetPeriodRepository.save(budgetPeriod);

        budgetRepository.saveAll(budgetList);

        //Build response


        Budget overallBudget = budgetList.stream()
                .filter(budget -> budget.getCategory() == null)
                .findFirst()
                .orElse(null);

        List<BudgetResponse.CategoryBudgetItem> categoryResponses =
                budgetList.stream()
                        .filter(budget -> budget.getCategory() != null)
                        .map(budget ->
                                BudgetResponse.CategoryBudgetItem.builder()
                                        .categoryName(
                                                budget.getCategory().getName()
                                        )
                                        .limit(
                                                budget.getBudgetLimit()
                                        )
                                        .build()
                        )
                        .toList();

        return BudgetResponse.builder()
                .id(budgetPeriod.getId())
                .overallBudget(
                        overallBudget != null
                                ? overallBudget.getBudgetLimit()
                                : null
                )
                .categoryBudgets(categoryResponses)
                .startDate(budgetPeriod.getStartDate())
                .endDate(budgetPeriod.getEndDate())
                .build();
    }
    public void deleteBudget() {
        UUID userId = currentUserService.getCurrentUserId();

        BudgetPeriod budgetPeriod = budgetPeriodRepository
                .findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        userId,
                        LocalDate.now(),
                        LocalDate.now()
                )
                .orElseThrow(() ->
                        new BudgetNotFoundException("No budget exists currently"));

        budgetPeriodRepository.delete(budgetPeriod);
    }
    private boolean checkDateValidity(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        return !startDate.isAfter(today) && !endDate.isBefore(startDate);
    }
    private boolean checkUpdatedDateValidity(
            LocalDate startDate,
            LocalDate endDate) {

        return !endDate.isBefore(startDate);
    }

}
