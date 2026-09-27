package com.spendwise.service;

import com.spendwise.dto.request.category.CreateCategoryRequest;
import com.spendwise.dto.request.category.DeleteCategoryRequest;
import com.spendwise.dto.request.category.GetCategoriesRequest;
import com.spendwise.dto.request.category.UpdateSystemCategoryRequest;
import com.spendwise.dto.response.category.CategoryResponse;
import com.spendwise.entity.Category;
import com.spendwise.entity.User;
import com.spendwise.enums.Role;
import com.spendwise.exception.CategoryExceptions.CategoryDoesNotExist;
import com.spendwise.exception.CategoryExceptions.CategoryInUseException;
import com.spendwise.exception.CategoryExceptions.DuplicateCategoryException;
import com.spendwise.exception.CategoryExceptions.InvalidCategoryException;
import com.spendwise.exception.CategoryExceptions.SystemCategoryException;
import com.spendwise.repository.BudgetRepository;
import com.spendwise.repository.CategoryRepository;
import com.spendwise.repository.ExpenseRepository;
import com.spendwise.repository.SettlementRepository;
import com.spendwise.repository.UserRepository;
import com.spendwise.security.SpendWiseUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private ExpenseRepository expenseRepository;

    @MockBean
    private SettlementRepository settlementRepository;

    @MockBean
    private BudgetRepository budgetRepository;

    private User currentUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {

        currentUser = createUser();
        anotherUser = createUser();

        authenticateAs(currentUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createCategory_shouldCreateCustomCategory() {

        String name = categoryName("Custom");

        CreateCategoryRequest request =
                CreateCategoryRequest.builder()
                        .name(name)
                        .icon("custom-icon")
                        .build();

        CategoryResponse response =
                categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals(name, response.getName());
        assertEquals("custom-icon", response.getIcon());
        assertFalse(response.isSystem());

        Category category =
                categoryRepository.findByNameAndUserId(
                        name,
                        currentUser.getId()
                ).orElseThrow();

        assertEquals(name, category.getName());
        assertEquals(currentUser.getId(), category.getUser().getId());
        assertFalse(category.isSystem());
    }

    @Test
    void createCategory_shouldRejectDuplicateCustomCategory() {

        String name = categoryName("Duplicate");

        createCustomCategory(currentUser, name);

        CreateCategoryRequest request =
                CreateCategoryRequest.builder()
                        .name(name)
                        .icon("icon")
                        .build();

        assertThrows(
                DuplicateCategoryException.class,
                () -> categoryService.createCategory(request)
        );
    }

    @Test
    void createCategory_shouldRejectDuplicateSystemCategory() {

        String name = categoryName("System");

        createSystemCategory(name);

        CreateCategoryRequest request =
                CreateCategoryRequest.builder()
                        .name(name)
                        .icon("icon")
                        .build();

        assertThrows(
                DuplicateCategoryException.class,
                () -> categoryService.createCategory(request)
        );
    }

    @Test
    void createCategory_shouldAllowSameCustomNameForDifferentUser() {

        String name = categoryName("Shared");

        createCustomCategory(anotherUser, name);

        CreateCategoryRequest request =
                CreateCategoryRequest.builder()
                        .name(name)
                        .icon("icon")
                        .build();

        CategoryResponse response =
                categoryService.createCategory(request);

        assertEquals(name, response.getName());
        assertFalse(response.isSystem());

        assertTrue(
                categoryRepository.existsByNameAndUserId(
                        name,
                        currentUser.getId()
                )
        );
    }

    @Test
    void getCategories_shouldReturnAllSystemAndCurrentUserCategories() {

        String customName = categoryName("Custom");
        String systemName = categoryName("System");

        createCustomCategory(currentUser, customName);
        createSystemCategory(systemName);

        List<CategoryResponse> responses =
                categoryService.getCategories(
                        GetCategoriesRequest.builder()
                                .filter("ALL")
                                .build()
                );

        assertTrue(
                responses.stream()
                        .anyMatch(response ->
                                response.getName().equals(customName)
                                        && !response.isSystem())
        );

        assertTrue(
                responses.stream()
                        .anyMatch(response ->
                                response.getName().equals(systemName)
                                        && response.isSystem())
        );
    }

    @Test
    void getCategories_shouldReturnOnlyCustomCategories() {

        String customName = categoryName("Custom");
        String systemName = categoryName("System");

        createCustomCategory(currentUser, customName);
        createSystemCategory(systemName);

        List<CategoryResponse> responses =
                categoryService.getCategories(
                        GetCategoriesRequest.builder()
                                .filter("CUSTOM")
                                .build()
                );

        assertTrue(
                responses.stream()
                        .anyMatch(response ->
                                response.getName().equals(customName))
        );

        assertFalse(
                responses.stream()
                        .anyMatch(response ->
                                response.getName().equals(systemName))
        );

        assertTrue(
                responses.stream()
                        .allMatch(response -> !response.isSystem())
        );
    }

    @Test
    void getCategories_shouldReturnOnlySystemCategories() {

        String customName = categoryName("Custom");
        String systemName = categoryName("System");

        createCustomCategory(currentUser, customName);
        createSystemCategory(systemName);

        List<CategoryResponse> responses =
                categoryService.getCategories(
                        GetCategoriesRequest.builder()
                                .filter("SYSTEM")
                                .build()
                );

        assertTrue(
                responses.stream()
                        .anyMatch(response ->
                                response.getName().equals(systemName))
        );

        assertFalse(
                responses.stream()
                        .anyMatch(response ->
                                response.getName().equals(customName))
        );

        assertTrue(
                responses.stream()
                        .allMatch(CategoryResponse::isSystem)
        );
    }

    @Test
    void getCategories_shouldRejectInvalidFilter() {

        GetCategoriesRequest request =
                GetCategoriesRequest.builder()
                        .filter("INVALID")
                        .build();

        assertThrows(
                InvalidCategoryException.class,
                () -> categoryService.getCategories(request)
        );
    }

    @Test
    void deleteCategory_shouldDeleteCustomCategory() {

        String name = categoryName("Delete");

        createCustomCategory(currentUser, name);

        when(
                expenseRepository.existsByCategoryIdAndUserId(
                        any(),
                        any()
                )
        ).thenReturn(false);

        when(
                settlementRepository.existsByCategoryId(any())
        ).thenReturn(false);

        CategoryResponse response =
                categoryService.deleteCategory(
                        DeleteCategoryRequest.builder()
                                .name(name)
                                .build()
                );

        assertEquals(name, response.getName());

        assertFalse(
                categoryRepository.existsByNameAndUserId(
                        name,
                        currentUser.getId()
                )
        );
    }

    @Test
    void deleteCategory_shouldRejectMissingCategory() {

        DeleteCategoryRequest request =
                DeleteCategoryRequest.builder()
                        .name(categoryName("Missing"))
                        .build();

        assertThrows(
                CategoryDoesNotExist.class,
                () -> categoryService.deleteCategory(request)
        );
    }

    @Test
    void deleteCategory_shouldRejectSystemCategory() {

        String name = categoryName("SystemDelete");

        createSystemCategory(name);

        assertThrows(
                SystemCategoryException.class,
                () -> categoryService.deleteCategory(
                        DeleteCategoryRequest.builder()
                                .name(name)
                                .build()
                )
        );
    }

    @Test
    void deleteCategory_shouldRejectCategoryInUseByExpense() {

        String name = categoryName("Used");

        Category category =
                createCustomCategory(currentUser, name);

        when(
                expenseRepository.existsByCategoryIdAndUserId(
                        category.getId(),
                        currentUser.getId()
                )
        ).thenReturn(true);

        assertThrows(
                CategoryInUseException.class,
                () -> categoryService.deleteCategory(
                        DeleteCategoryRequest.builder()
                                .name(name)
                                .build()
                )
        );

        assertTrue(
                categoryRepository.existsByNameAndUserId(
                        name,
                        currentUser.getId()
                )
        );
    }

    @Test
    void createSystemCategory_shouldCreateSystemCategory() {

        String name = categoryName("NewSystem");

        CategoryResponse response =
                categoryService.createSystemCategory(
                        CreateCategoryRequest.builder()
                                .name(name)
                                .icon("system-icon")
                                .build()
                );

        assertEquals(name, response.getName());
        assertEquals("system-icon", response.getIcon());
        assertTrue(response.isSystem());

        Category category =
                categoryRepository
                        .findByNameAndIsSystemTrue(name)
                        .orElseThrow();

        assertTrue(category.isSystem());
        assertNull(category.getUser());
    }

    @Test
    void createSystemCategory_shouldRejectDuplicate() {

        String name = categoryName("DuplicateSystem");

        createSystemCategory(name);

        assertThrows(
                DuplicateCategoryException.class,
                () -> categoryService.createSystemCategory(
                        CreateCategoryRequest.builder()
                                .name(name)
                                .icon("icon")
                                .build()
                )
        );
    }

    @Test
    void updateSystemCategory_shouldUpdateNameAndIcon() {

        String oldName = categoryName("OldSystem");
        String newName = categoryName("NewSystem");

        Category category = createSystemCategory(oldName);

        when(
                expenseRepository.existsByCategoryId(category.getId())
        ).thenReturn(false);

        CategoryResponse response =
                categoryService.updateSystemCategory(
                        category.getId(),
                        UpdateSystemCategoryRequest.builder()
                                .name(newName)
                                .icon("new-icon")
                                .build()
                );

        assertEquals(newName, response.getName());
        assertEquals("new-icon", response.getIcon());
        assertTrue(response.isSystem());

        Category updated =
                categoryRepository.findById(category.getId())
                        .orElseThrow();

        assertEquals(newName, updated.getName());
        assertEquals("new-icon", updated.getIcon());
    }

    @Test
    void updateSystemCategory_shouldRejectMissingCategory() {

        UUID missingId = UUID.randomUUID();

        assertThrows(
                CategoryDoesNotExist.class,
                () -> categoryService.updateSystemCategory(
                        missingId,
                        UpdateSystemCategoryRequest.builder()
                                .name(categoryName("NewName"))
                                .icon("icon")
                                .build()
                )
        );
    }

    @Test
    void updateSystemCategory_shouldRejectDuplicateName() {

        String firstName = categoryName("SystemA");
        String secondName = categoryName("SystemB");

        Category first = createSystemCategory(firstName);
        createSystemCategory(secondName);

        assertThrows(
                DuplicateCategoryException.class,
                () -> categoryService.updateSystemCategory(
                        first.getId(),
                        UpdateSystemCategoryRequest.builder()
                                .name(secondName)
                                .icon("icon")
                                .build()
                )
        );
    }

    @Test
    void updateSystemCategory_shouldRejectCategoryInUse() {

        String oldName = categoryName("UsedSystem");
        String newName = categoryName("RenamedSystem");

        Category category = createSystemCategory(oldName);

        when(
                expenseRepository.existsByCategoryId(category.getId())
        ).thenReturn(true);

        assertThrows(
                CategoryInUseException.class,
                () -> categoryService.updateSystemCategory(
                        category.getId(),
                        UpdateSystemCategoryRequest.builder()
                                .name(newName)
                                .icon("icon")
                                .build()
                )
        );
    }

    @Test
    void deleteSystemCategory_shouldDeleteUnusedSystemCategory() {

        String name = categoryName("DeleteSystem");

        Category category = createSystemCategory(name);

        when(
                expenseRepository.existsByCategoryId(category.getId())
        ).thenReturn(false);

        when(
                settlementRepository.existsByCategoryId(category.getId())
        ).thenReturn(false);

        when(
                budgetRepository.existsByCategoryId(category.getId())
        ).thenReturn(false);

        CategoryResponse response =
                categoryService.deleteSystemCategory(category.getId());

        assertEquals(name, response.getName());

        assertFalse(
                categoryRepository.findById(category.getId()).isPresent()
        );
    }

    @Test
    void deleteSystemCategory_shouldRejectMissingCategory() {

        UUID missingId = UUID.randomUUID();

        assertThrows(
                CategoryDoesNotExist.class,
                () -> categoryService.deleteSystemCategory(missingId)
        );
    }

    @Test
    void deleteSystemCategory_shouldRejectCategoryInUse() {

        String name = categoryName("UsedDeleteSystem");

        Category category = createSystemCategory(name);

        when(
                expenseRepository.existsByCategoryId(category.getId())
        ).thenReturn(true);

        assertThrows(
                CategoryInUseException.class,
                () -> categoryService.deleteSystemCategory(category.getId())
        );

        assertTrue(
                categoryRepository.findById(category.getId()).isPresent()
        );
    }

    private Category createCustomCategory(
            User user,
            String name
    ) {

        Category category = Category.builder()
                .name(name)
                .icon("test-icon")
                .isSystem(false)
                .user(user)
                .build();

        return categoryRepository.save(category);
    }

    private Category createSystemCategory(String name) {

        Category category = Category.builder()
                .name(name)
                .icon("system-icon")
                .isSystem(true)
                .user(null)
                .build();

        return categoryRepository.save(category);
    }

    private String categoryName(String prefix) {

        return prefix + "_" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8);
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
                .username("category_test_" + unique)
                .name("Category Test User")
                .email("category_test_" + unique + "@example.com")
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

