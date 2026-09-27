package com.spendwise.service;

import com.spendwise.dto.request.budget.UpdateBudgetRequest;
import com.spendwise.dto.request.budget.createBudgetRequest;
import com.spendwise.dto.response.budget.BudgetResponse;
import com.spendwise.entity.Budget;
import com.spendwise.entity.BudgetPeriod;
import com.spendwise.entity.Category;
import com.spendwise.entity.User;
import com.spendwise.enums.Role;
import com.spendwise.exception.BudgetExceptions.BudgetAlreadyExistsException;
import com.spendwise.exception.BudgetExceptions.BudgetNotFoundException;
import com.spendwise.exception.BudgetExceptions.DuplicateBudgetCategoryException;
import com.spendwise.exception.BudgetExceptions.InvalidBudgetLimitException;
import com.spendwise.exception.BudgetExceptions.InvalidStartandEndDateException;
import com.spendwise.exception.CategoryExceptions.CategoryDoesNotExist;
import com.spendwise.repository.BudgetPeriodRepository;
import com.spendwise.repository.BudgetRepository;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.UserRepository;
import com.spendwise.security.SpendWiseUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BudgetServicesTest {

    @Autowired
    private BudgetServices budgetServices;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private BudgetPeriodRepository budgetPeriodRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User currentUser;
    private User anotherUser;

    private Category foodCategory;
    private Category travelCategory;

    @BeforeEach
    void setUp() {

        currentUser = createUser();
        anotherUser = createUser();

        foodCategory = createCategory("Food");
        travelCategory = createCategory("Travel");

        authenticateAs(currentUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createBudget_shouldCreateOverallAndCategoryBudgets() {

        createBudgetRequest request =
                createRequest(
                        new BigDecimal("10000"),
                        List.of(
                                categoryItem("Food", "3000"),
                                categoryItem("Travel", "2000")
                        ),
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(30)
                );

        BudgetResponse response =
                budgetServices.createBudget(request);

        assertNotNull(response);
        assertNotNull(response.getId());

        assertEquals(
                0,
                new BigDecimal("10000")
                        .compareTo(response.getOverallBudget())
        );

        assertEquals(
                2,
                response.getCategoryBudgets().size()
        );

        assertEquals(
                LocalDate.now().minusDays(1),
                response.getStartDate()
        );

        assertEquals(
                LocalDate.now().plusDays(30),
                response.getEndDate()
        );

        BudgetPeriod period =
                budgetPeriodRepository
                        .findById(response.getId())
                        .orElseThrow();

        assertEquals(currentUser.getId(), period.getUser().getId());

        List<Budget> budgets =
                budgetRepository.findByBudgetPeriodId(period.getId());

        assertEquals(3, budgets.size());

        assertTrue(
                budgets.stream()
                        .anyMatch(budget ->
                                budget.getCategory() == null
                                        && budget.getBudgetLimit()
                                        .compareTo(
                                                new BigDecimal("10000")
                                        ) == 0)
        );

        assertTrue(
                response.getCategoryBudgets()
                        .stream()
                        .anyMatch(item ->
                                item.getCategoryName().equals("Food")
                                        && item.getLimit()
                                        .compareTo(
                                                new BigDecimal("3000")
                                        ) == 0)
        );
    }

    @Test
    void createBudget_shouldAllowOverallBudgetWithoutCategories() {

        createBudgetRequest request =
                createRequest(
                        new BigDecimal("5000"),
                        List.of(),
                        LocalDate.now(),
                        LocalDate.now().plusDays(30)
                );

        BudgetResponse response =
                budgetServices.createBudget(request);

        assertNotNull(response);
        assertEquals(
                0,
                new BigDecimal("5000")
                        .compareTo(response.getOverallBudget())
        );

        assertTrue(
                response.getCategoryBudgets().isEmpty()
        );
    }

    @Test
    void createBudget_shouldAllowCategoryBudgetsWithoutOverallBudget() {

        createBudgetRequest request =
                createRequest(
                        null,
                        List.of(
                                categoryItem("Food", "2000"),
                                categoryItem("Travel", "1500")
                        ),
                        LocalDate.now(),
                        LocalDate.now().plusDays(30)
                );

        BudgetResponse response =
                budgetServices.createBudget(request);

        assertNotNull(response);
        assertNull(response.getOverallBudget());
        assertEquals(2, response.getCategoryBudgets().size());
    }

    @Test
    void createBudget_shouldRejectActiveExistingBudget() {

        createBudgetRequest firstRequest =
                createRequest(
                        new BigDecimal("5000"),
                        List.of(),
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(30)
                );

        budgetServices.createBudget(firstRequest);

        createBudgetRequest secondRequest =
                createRequest(
                        new BigDecimal("7000"),
                        List.of(),
                        LocalDate.now().minusDays(2),
                        LocalDate.now().plusDays(40)
                );

        assertThrows(
                BudgetAlreadyExistsException.class,
                () -> budgetServices.createBudget(secondRequest)
        );
    }

    @Test
    void createBudget_shouldRejectStartDateAfterToday() {

        createBudgetRequest request =
                createRequest(
                        new BigDecimal("5000"),
                        List.of(),
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(30)
                );

        assertThrows(
                InvalidStartandEndDateException.class,
                () -> budgetServices.createBudget(request)
        );
    }

    @Test
    void createBudget_shouldRejectEndDateBeforeStartDate() {

        createBudgetRequest request =
                createRequest(
                        new BigDecimal("5000"),
                        List.of(),
                        LocalDate.now(),
                        LocalDate.now().minusDays(1)
                );

        assertThrows(
                InvalidStartandEndDateException.class,
                () -> budgetServices.createBudget(request)
        );
    }

    @Test
    void createBudget_shouldRejectDuplicateCategory() {

        createBudgetRequest request =
                createRequest(
                        new BigDecimal("5000"),
                        List.of(
                                categoryItem("Food", "2000"),
                                categoryItem("food", "1000")
                        ),
                        LocalDate.now(),
                        LocalDate.now().plusDays(30)
                );

        assertThrows(
                DuplicateBudgetCategoryException.class,
                () -> budgetServices.createBudget(request)
        );
    }

    @Test
    void createBudget_shouldRejectUnknownCategory() {

        createBudgetRequest request =
                createRequest(
                        new BigDecimal("5000"),
                        List.of(
                                categoryItem("DoesNotExist", "2000")
                        ),
                        LocalDate.now(),
                        LocalDate.now().plusDays(30)
                );

        assertThrows(
                CategoryDoesNotExist.class,
                () -> budgetServices.createBudget(request)
        );
    }

    @Test
    void createBudget_shouldRejectCategoryTotalAboveOverallBudget() {

        createBudgetRequest request =
                createRequest(
                        new BigDecimal("5000"),
                        List.of(
                                categoryItem("Food", "3000"),
                                categoryItem("Travel", "3000")
                        ),
                        LocalDate.now(),
                        LocalDate.now().plusDays(30)
                );

        assertThrows(
                InvalidBudgetLimitException.class,
                () -> budgetServices.createBudget(request)
        );
    }

    @Test
    void getCurrentBudget_shouldReturnActiveBudget() {

        createBudgetRequest request =
                createRequest(
                        new BigDecimal("10000"),
                        List.of(
                                categoryItem("Food", "3000")
                        ),
                        LocalDate.now().minusDays(5),
                        LocalDate.now().plusDays(30)
                );

        BudgetResponse created =
                budgetServices.createBudget(request);

        BudgetResponse response =
                budgetServices.getCurrentBudget();

        assertEquals(created.getId(), response.getId());

        assertEquals(
                0,
                new BigDecimal("10000")
                        .compareTo(response.getOverallBudget())
        );

        assertEquals(1, response.getCategoryBudgets().size());

        assertEquals(
                "Food",
                response.getCategoryBudgets()
                        .get(0)
                        .getCategoryName()
        );
    }

    @Test
    void getCurrentBudget_shouldRejectWhenNoBudgetExists() {

        assertThrows(
                BudgetNotFoundException.class,
                () -> budgetServices.getCurrentBudget()
        );
    }

    @Test
    void updateBudget_shouldUpdateOverallBudget() {

        BudgetResponse created =
                budgetServices.createBudget(
                        createRequest(
                                new BigDecimal("5000"),
                                List.of(
                                        categoryItem("Food", "2000")
                                ),
                                LocalDate.now().minusDays(1),
                                LocalDate.now().plusDays(30)
                        )
                );

        BudgetResponse response =
                budgetServices.updateBudget(
                        UpdateBudgetRequest.builder()
                                .overallBudget(new BigDecimal("8000"))
                                .build()
                );

        assertEquals(
                0,
                new BigDecimal("8000")
                        .compareTo(response.getOverallBudget())
        );

        assertEquals(created.getId(), response.getId());
    }

    @Test
    void updateBudget_shouldUpdateExistingCategoryBudget() {

        budgetServices.createBudget(
                createRequest(
                        new BigDecimal("10000"),
                        List.of(
                                categoryItem("Food", "2000")
                        ),
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(30)
                )
        );

        BudgetResponse response =
                budgetServices.updateBudget(
                        UpdateBudgetRequest.builder()
                                .categoryBudgets(
                                        List.of(
                                                updateCategoryItem(
                                                        "Food",
                                                        "4000"
                                                )
                                        )
                                )
                                .build()
                );

        BudgetResponse.CategoryBudgetItem food =
                response.getCategoryBudgets()
                        .stream()
                        .filter(item ->
                                item.getCategoryName()
                                        .equals("Food"))
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("4000")
                        .compareTo(food.getLimit())
        );
    }

    @Test
    void updateBudget_shouldAddNewCategoryBudget() {

        budgetServices.createBudget(
                createRequest(
                        new BigDecimal("10000"),
                        List.of(
                                categoryItem("Food", "2000")
                        ),
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(30)
                )
        );

        BudgetResponse response =
                budgetServices.updateBudget(
                        UpdateBudgetRequest.builder()
                                .categoryBudgets(
                                        List.of(
                                                updateCategoryItem(
                                                        "Travel",
                                                        "3000"
                                                )
                                        )
                                )
                                .build()
                );

        assertEquals(2, response.getCategoryBudgets().size());

        assertTrue(
                response.getCategoryBudgets()
                        .stream()
                        .anyMatch(item ->
                                item.getCategoryName()
                                        .equals("Travel")
                                        && item.getLimit()
                                        .compareTo(
                                                new BigDecimal("3000")
                                        ) == 0)
        );
    }

    @Test
    void updateBudget_shouldRejectDuplicateCategories() {

        budgetServices.createBudget(
                createRequest(
                        new BigDecimal("10000"),
                        List.of(
                                categoryItem("Food", "2000")
                        ),
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(30)
                )
        );

        UpdateBudgetRequest request =
                UpdateBudgetRequest.builder()
                        .categoryBudgets(
                                List.of(
                                        updateCategoryItem(
                                                "Food",
                                                "3000"
                                        ),
                                        updateCategoryItem(
                                                "food",
                                                "4000"
                                        )
                                )
                        )
                        .build();

        assertThrows(
                DuplicateBudgetCategoryException.class,
                () -> budgetServices.updateBudget(request)
        );
    }

    @Test
    void updateBudget_shouldRejectCategoryTotalAboveOverallBudget() {

        budgetServices.createBudget(
                createRequest(
                        new BigDecimal("5000"),
                        List.of(
                                categoryItem("Food", "2000")
                        ),
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(30)
                )
        );

        UpdateBudgetRequest request =
                UpdateBudgetRequest.builder()
                        .categoryBudgets(
                                List.of(
                                        updateCategoryItem(
                                                "Food",
                                                "6000"
                                        )
                                )
                        )
                        .build();

        assertThrows(
                InvalidBudgetLimitException.class,
                () -> budgetServices.updateBudget(request)
        );
    }

    @Test
    void updateBudget_shouldRejectInvalidUpdatedDates() {

        budgetServices.createBudget(
                createRequest(
                        new BigDecimal("5000"),
                        List.of(),
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(30)
                )
        );

        UpdateBudgetRequest request =
                UpdateBudgetRequest.builder()
                        .startDate(LocalDate.now().plusDays(20))
                        .endDate(LocalDate.now().plusDays(10))
                        .build();

        assertThrows(
                InvalidStartandEndDateException.class,
                () -> budgetServices.updateBudget(request)
        );
    }

    @Test
    void deleteBudget_shouldDeleteCurrentBudget() {

        BudgetResponse created =
                budgetServices.createBudget(
                        createRequest(
                                new BigDecimal("5000"),
                                List.of(
                                        categoryItem("Food", "2000")
                                ),
                                LocalDate.now().minusDays(1),
                                LocalDate.now().plusDays(30)
                        )
                );

        budgetServices.deleteBudget();

        assertFalse(
                budgetPeriodRepository
                        .findById(created.getId())
                        .isPresent()
        );
    }

    @Test
    void deleteBudget_shouldRejectWhenNoBudgetExists() {

        assertThrows(
                BudgetNotFoundException.class,
                () -> budgetServices.deleteBudget()
        );
    }

    @Test
    void budgetShouldBelongToCurrentUser() {

        budgetServices.createBudget(
                createRequest(
                        new BigDecimal("5000"),
                        List.of(),
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(30)
                )
        );

        SecurityContextHolder.clearContext();
        authenticateAs(anotherUser);

        assertThrows(
                BudgetNotFoundException.class,
                () -> budgetServices.getCurrentBudget()
        );
    }

    private createBudgetRequest createRequest(
            BigDecimal overallBudget,
            List<createBudgetRequest.CategoryBudgetItem> categories,
            LocalDate startDate,
            LocalDate endDate
    ) {

        return createBudgetRequest.builder()
                .overallBudget(overallBudget)
                .categoryBudgets(categories)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    private createBudgetRequest.CategoryBudgetItem categoryItem(
            String name,
            String limit
    ) {

        return createBudgetRequest.CategoryBudgetItem.builder()
                .categoryName(name)
                .limit(new BigDecimal(limit))
                .build();
    }

    private UpdateBudgetRequest.CategoryBudgetItem updateCategoryItem(
            String name,
            String limit
    ) {

        return UpdateBudgetRequest.CategoryBudgetItem.builder()
                .categoryName(name)
                .limit(new BigDecimal(limit))
                .build();
    }

    private Category createCategory(String name) {

        String uniqueName =
                name + "_" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8);

        Category category = Category.builder()
                .name(uniqueName)
                .icon("test-icon")
                .isSystem(false)
                .user(currentUser)
                .build();

        /*
         * The service looks categories up using the supplied name,
         * so the fixtures need to use the exact generated name.
         *
         * For the tests above we therefore need the categories to
         * actually be named Food / Travel. This method is intentionally
         * overridden below by createNamedCategory().
         */

        return categoryRepository.save(category);
    }

    private Category createNamedCategory(String name) {

        Category category = Category.builder()
                .name(name)
                .icon("test-icon")
                .isSystem(false)
                .user(currentUser)
                .build();

        return categoryRepository.save(category);
    }

    private User createUser() {

        String unique = UUID.randomUUID().toString();

        String phone = String.format(
                "9%09d",
                Math.abs(
                        UUID.randomUUID().getMostSignificantBits()
                                % 1_000_000_000L
                )
        );

        User user = User.builder()
                .username("budget_test_" + unique)
                .name("Budget Test User")
                .email("budget_test_" + unique + "@example.com")
                .phoneNumber(phone)
                .role(Role.USER)
                .emailVerified(true)
                .build();

        user.updatePasswordHash("password");

        return userRepository.save(user);
    }

    private void authenticateAs(User user) {

        SpendWiseUserDetails userDetails =
                new SpendWiseUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}
