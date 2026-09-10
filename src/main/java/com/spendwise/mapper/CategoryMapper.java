package com.spendwise.mapper;

import com.spendwise.dto.request.category.CreateCategoryRequest;
import com.spendwise.dto.response.category.CategoryResponse;
import com.spendwise.entity.Category;
import com.spendwise.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "isSystem", source = "system")
    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "icon", source = "icon")
    @Mapping(target = "isSystem", source = "isSystem")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(
            CreateCategoryRequest request,
            User user,
            boolean isSystem,
            String name,
            String icon
            );
}