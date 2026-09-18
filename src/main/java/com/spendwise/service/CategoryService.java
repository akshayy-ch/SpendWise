package com.spendwise.service;

import com.spendwise.dto.request.category.CreateCategoryRequest;
import com.spendwise.dto.request.category.DeleteCategoryRequest;
import com.spendwise.dto.request.category.GetCategoriesRequest;
import com.spendwise.dto.request.category.UpdateSystemCategoryRequest;
import com.spendwise.dto.response.category.CategoryResponse;
import com.spendwise.entity.Category;
import com.spendwise.entity.User;
import com.spendwise.enums.CategoryFilter;
import com.spendwise.exception.CategoryExceptions.*;
import com.spendwise.mapper.CategoryMapper;
import com.spendwise.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryMapper categoryMapper;
    private final SettlementRepository settlementRepository;
    private final BudgetRepository budgetRepository;

    private final CurrentUserService currentUserService;

    public CategoryResponse createCategory(CreateCategoryRequest request){

        UUID userId = currentUserService.getCurrentUserId();

        if (categoryRepository.existsByNameAndUserId(request.getName(), userId)
                || categoryRepository.existsByNameAndUserIsNull(request.getName())) {

            throw new DuplicateCategoryException(
                    "Category with same name already exists"
            );
        }

        User user = userRepository.getReferenceById(userId);

        Category category = categoryMapper.toEntity(
                request,
                user,
                false,
                request.getName(),
                request.getIcon()
        );

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    public List<CategoryResponse> getCategories(GetCategoriesRequest request){

        UUID userId = currentUserService.getCurrentUserId();

        CategoryFilter categoryFilter;
        try {
            categoryFilter = CategoryFilter.valueOf(request.getFilter().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCategoryException("Category does not exist");
        }

        List<Category> categories = List.of();
        switch (categoryFilter) {
            case ALL -> categories =
                    categoryRepository.findByIsSystemTrueOrUserId(userId);

            case CUSTOM -> categories =
                    categoryRepository.findByUserId(userId);

            case SYSTEM -> categories =
                    categoryRepository.findByIsSystemTrue();
        }

        return categoryMapper.toResponseList(categories);
    }

    public CategoryResponse deleteCategory(DeleteCategoryRequest request){
        UUID userId = currentUserService.getCurrentUserId();

        Category category = categoryRepository
                .findAvailableCategory(request.getName(), userId)
                .orElseThrow(() ->
                        new CategoryDoesNotExist("Category does not exist" + request.getName()));

        if (category.isSystem()) {
            throw new SystemCategoryException(
                    "System categories cannot be deleted."
            );
        }
        boolean usedBySettlement = settlementRepository.existsByCategoryId(category.getId());

        if (expenseRepository.existsByCategoryIdAndUserId(category.getId(), userId) || usedBySettlement) {

            throw new CategoryInUseException(
                    "Cannot delete category because it is currently in use."
            );
        }

        categoryRepository.delete(category);
        return CategoryResponse.builder()
                .name(category.getName())
                .build();
    }

    public CategoryResponse createSystemCategory(CreateCategoryRequest request) {

        if (categoryRepository.existsByNameAndUserIsNull(request.getName())) {
            throw new DuplicateCategoryException("System category with same name already exists");
        }
        Category category = categoryMapper.toEntity(
                request,
                null,
                true,
                request.getName(),
                request.getIcon()
        );
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }
    public CategoryResponse updateSystemCategory(UUID categoryId, UpdateSystemCategoryRequest request) {

        Category category = categoryRepository.findByIdAndIsSystemTrue(categoryId).orElseThrow(()-> new CategoryDoesNotExist("Category does not exist"));

        if (!category.getName().equalsIgnoreCase(request.getName())) {
            if (categoryRepository.existsByNameAndUserIsNull(request.getName())) {
                throw new DuplicateCategoryException("System category with same name already exists");
            }
            if (expenseRepository.existsByCategoryId(categoryId)) {
                throw new CategoryInUseException(
                        "Cannot update a category that is already in use"
                );
            }
        }
        category.setName(request.getName());
        category.setIcon(request.getIcon());

        categoryRepository.save(category);

        return categoryMapper.toResponse(category);
    }
    public CategoryResponse deleteSystemCategory(UUID categoryId){
        Category category = categoryRepository.findByIdAndIsSystemTrue(categoryId).orElseThrow(()-> new CategoryDoesNotExist("Category does not exist"));

        if (expenseRepository.existsByCategoryId(categoryId)) {
            throw new CategoryInUseException(
                    "Cannot delete a category that is already in use"
            );
        }
        if (expenseRepository.existsByCategoryId(categoryId)
                || settlementRepository.existsByCategoryId(categoryId)
                || budgetRepository.existsByCategoryId(categoryId)) {

            throw new CategoryInUseException(
                    "Cannot delete a category that is already in use"
            );
        }
        categoryRepository.delete(category);
        return categoryMapper.toResponse(category);
    }
}