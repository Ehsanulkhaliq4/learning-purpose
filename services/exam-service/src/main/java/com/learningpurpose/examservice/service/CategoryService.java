package com.learningpurpose.examservice.service;

import com.learningpurpose.examservice.dto.CategoryDto;
import com.learningpurpose.examservice.model.Category;

import java.util.List;

public interface CategoryService {
    Category addCategory(CategoryDto categoryDto);
    Category updateCategory(Long id, CategoryDto categoryDto);
    List<Category> getCategories();
    Category getCategory(Long categoryId);
    void deleteCategory(Long categoryId);
}