package com.spendwise.controller;

import com.spendwise.dto.request.category.CreateCategoryRequest;
import com.spendwise.dto.request.category.UpdateSystemCategoryRequest;
import com.spendwise.dto.response.category.CategoryResponse;
import com.spendwise.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping()
    public ResponseEntity<CategoryResponse> createSystemCategory(
            @Valid @RequestBody CreateCategoryRequest request) {

        CategoryResponse response =
                categoryService.createSystemCategory(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateSystemCategory(@PathVariable UUID categoryId,
                                                                 @Valid @RequestBody UpdateSystemCategoryRequest request){
        CategoryResponse response = categoryService.updateSystemCategory(categoryId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> deleteSystemCategory(@PathVariable UUID categoryId){
        CategoryResponse response = categoryService.deleteSystemCategory(categoryId);
        return ResponseEntity.ok(response);
    }
}
