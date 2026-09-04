package com.spendwise.controller;

import com.spendwise.dto.request.category.CreateCategoryRequest;
import com.spendwise.dto.request.category.DeleteCategoryRequest;
import com.spendwise.dto.request.category.GetCategoriesRequest;
import com.spendwise.dto.response.category.CategoryResponse;
import com.spendwise.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/createCategory")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request){
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/getCategories")
    public ResponseEntity<List<CategoryResponse>> getCategories(@Valid GetCategoriesRequest request) {
        return ResponseEntity.ok(categoryService.getCategories(request));
    }

    @DeleteMapping("/deleteCategory")
    public ResponseEntity<CategoryResponse> deleteCategory(@Valid @RequestBody DeleteCategoryRequest request){
        CategoryResponse response = categoryService.deleteCategory(request);
        return ResponseEntity.ok(response);
    }
}
