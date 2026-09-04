package com.spendwise.service;

import com.spendwise.dto.request.expenseShare.CreateExpenseShareRequest;
import com.spendwise.dto.response.expenseShare.ExpenseShareResponse;
import com.spendwise.entity.Expense;
import com.spendwise.entity.ExpenseShare;
import com.spendwise.entity.Group;
import com.spendwise.entity.GroupMember;
import com.spendwise.entity.User;
import com.spendwise.enums.ExpenseShareStatus;
import com.spendwise.enums.ExpenseStatus;
import com.spendwise.enums.GroupMemberStatus;
import com.spendwise.enums.GroupStatus;
import com.spendwise.enums.SplitType;
import com.spendwise.exception.ExpenseException.VoidedExpenseException;
import com.spendwise.exception.ExpenseException.ExpenseDoesNotExist;
import com.spendwise.exception.ExpenseShareExceptions.InvalidSplitException;
import com.spendwise.exception.GroupExceptions.ArchivedGroupException;
import com.spendwise.exception.GroupExceptions.GroupDoesNotExist;
import com.spendwise.exception.GroupExceptions.UnauthorizedGroupActionException;
import com.spendwise.exception.GroupMemberExceptions.InactiveGroupMemberException;
import com.spendwise.exception.GroupMemberExceptions.NotGroupMemberException;
import com.spendwise.exception.GroupMemberExceptions.UserDoesNotExist;
import com.spendwise.mapper.ExpenseShareMapper;
import com.spendwise.repository.ExpenseRepository;
import com.spendwise.repository.ExpenseShareRepository;
import com.spendwise.repository.GroupMemberRepository;
import com.spendwise.repository.GroupRepository;
import com.spendwise.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseShareService {

    private final ExpenseShareRepository expenseShareRepository;
    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final ExpenseShareMapper expenseShareMapper;

    @Transactional
    public List<ExpenseShareResponse> createShares(UUID expenseId, UUID groupId, CreateExpenseShareRequest request) {

        UUID currentUserId = currentUserService.getCurrentUserId();

        Expense expense = expenseRepository.findById(expenseId).orElseThrow(() -> new ExpenseDoesNotExist("Expense not found"));

        if (!expense.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedGroupActionException("Only the expense creator can create shares");
        }

        if (expense.getStatus() != ExpenseStatus.ACTIVE) {
            throw new VoidedExpenseException("Shares cannot be created for a voided expense");
        }

        Group group = groupRepository.findById(groupId).orElseThrow(() -> new GroupDoesNotExist("Group not found"));

        if (group.getStatus() != GroupStatus.ACTIVE) {
            throw new ArchivedGroupException("Shares cannot be created for an archived group");
        }

        GroupMember currentMember = groupMemberRepository.findByIdGroupIdAndIdUserId(groupId, currentUserId).orElseThrow(() -> new NotGroupMemberException("You are not a member of this group"));

        if (currentMember.getStatus() != GroupMemberStatus.ACTIVE) {
            throw new InactiveGroupMemberException("You are not an active member of this group");
        }

        List<UUID> userIds = request.getUserIds();

        List<User> users = new ArrayList<>();

        for (UUID userId : userIds) {

            if (userId.equals(currentUserId)) {
                throw new InvalidSplitException("Expense creator cannot be included in expense shares");
            }

            User user = userRepository.findById(userId).orElseThrow(() -> new UserDoesNotExist("User not found: " + userId));

            GroupMember member = groupMemberRepository.findByIdGroupIdAndIdUserId(groupId, userId).orElseThrow(() -> new UnauthorizedGroupActionException("User is not a member of this group: " + userId));

            if (member.getStatus() != GroupMemberStatus.ACTIVE) {
                throw new UnauthorizedGroupActionException("User is not an active member of this group: " + userId);
            }
            users.add(user);
        }

        List<BigDecimal> amounts;

        switch (request.getSplitType()) {

            case EQUAL -> amounts =
                    calculateEqualShares(
                            expense.getAmount(),
                            users.size()
                    );

            case PERCENTAGE -> {

                if (request.getPercentages() == null ||
                        request.getPercentages().size() != users.size()) {

                    throw new InvalidSplitException(
                            "Number of percentages must match number of users"
                    );
                }

                amounts = calculatePercentageShares(
                        expense.getAmount(),
                        request.getPercentages()
                );
            }

            case RANDOM -> amounts =
                    calculateRandomShares(
                            expense.getAmount(),
                            users.size()
                    );

            default -> throw new InvalidSplitException(
                    "Unsupported split type"
            );
        }

        List<ExpenseShare> shares = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {

            ExpenseShare share = ExpenseShare.builder()
                    .originalAmount(amounts.get(i))
                    .remainingAmount(amounts.get(i))
                    .status(ExpenseShareStatus.PENDING)
                    .user(users.get(i))
                    .expense(expense)
                    .group(group)
                    .build();

            if (request.getSplitType() == SplitType.PERCENTAGE) {
                share.setPercentage(
                        request.getPercentages().get(i)
                );
            }

            shares.add(share);
        }

        List<ExpenseShare> savedShares =
                expenseShareRepository.saveAll(shares);

        return expenseShareMapper.toResponseList(savedShares);
    }

    private List<BigDecimal> calculateEqualShares(BigDecimal totalAmount, int numberOfUsers) {

        if (numberOfUsers <= 0) {
            throw new InvalidSplitException("At least one user is required");
        }
        BigDecimal[] result = new BigDecimal[numberOfUsers];
        BigDecimal baseAmount = totalAmount.divide(BigDecimal.valueOf(numberOfUsers), 2, RoundingMode.DOWN);

        BigDecimal assigned = baseAmount.multiply(BigDecimal.valueOf(numberOfUsers));

        BigDecimal remainder = totalAmount.subtract(assigned);

        for (int i = 0; i < numberOfUsers; i++) {
            result[i] = baseAmount;
        }
        result[numberOfUsers - 1] = result[numberOfUsers - 1].add(remainder);
        return List.of(result);
    }

    private List<BigDecimal> calculatePercentageShares(BigDecimal totalAmount, List<BigDecimal> percentages) {

        if (percentages == null || percentages.isEmpty()) {
            throw new InvalidSplitException("Percentages are required for percentage split");
        }

        BigDecimal totalPercentage = percentages.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new InvalidSplitException("Percentages must add up to 100");
        }

        List<BigDecimal> result = new ArrayList<>();

        BigDecimal assigned = BigDecimal.ZERO;


        for (int i = 0; i < percentages.size(); i++) {

            BigDecimal percentage = percentages.get(i);

            if (percentage == null ||percentage.compareTo(BigDecimal.ZERO) <= 0 ||percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new InvalidSplitException("Each percentage must be greater than 0 " + "and less than or equal to 100");
            }

            BigDecimal amount = totalAmount.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);

            result.add(amount);
            assigned = assigned.add(amount);
        }
        BigDecimal remainder = totalAmount.subtract(assigned);

        result.set(result.size() - 1, result.get(result.size() - 1).add(remainder) );

        return result;
    }

    private List<BigDecimal> calculateRandomShares(BigDecimal totalAmount, int numberOfUsers) {
        if (numberOfUsers <= 0) {
            throw new InvalidSplitException("At least one user is required");
        }

        List<BigDecimal> weights = new ArrayList<>();

        BigDecimal totalWeight = BigDecimal.ZERO;

        for (int i = 0; i < numberOfUsers; i++) {
            BigDecimal weight = BigDecimal.valueOf(Math.random() * 100 + 1);
            weights.add(weight);
            totalWeight = totalWeight.add(weight);
        }
        List<BigDecimal> result = new ArrayList<>();

        BigDecimal assigned = BigDecimal.ZERO;

        for (int i = 0; i < numberOfUsers; i++) {

            BigDecimal amount = totalAmount.multiply(weights.get(i)).divide(totalWeight, 2, RoundingMode.DOWN);
            result.add(amount);
            assigned = assigned.add(amount);
        }
        BigDecimal remainder = totalAmount.subtract(assigned);

        result.set(result.size() - 1, result.get(result.size() - 1).add(remainder));
        return result;
    }

    @Transactional
    public List<ExpenseShareResponse> getExpenseSharesByExpense(UUID expenseId) {

        List<ExpenseShare> shares =
                expenseShareRepository.findByExpenseId(expenseId);

        return shares.stream()
                .map(this::mapToResponse)
                .toList();
    }
    @Transactional
    public List<ExpenseShareResponse> getMyExpenseShares() {
        UUID userId = currentUserService.getCurrentUserId();

        List<ExpenseShare> shares =
                expenseShareRepository.findByUserId(userId);

        return shares.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ExpenseShareResponse mapToResponse(ExpenseShare share) {
        return expenseShareMapper.toResponse(share);
    }
}