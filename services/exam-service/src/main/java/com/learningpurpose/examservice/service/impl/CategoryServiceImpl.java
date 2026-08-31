package com.learningpurpose.examservice.service.impl;

import com.learningpurpose.examservice.dto.CategoryDto;
import com.learningpurpose.examservice.exception.ResourceNotFoundException;
import com.learningpurpose.examservice.model.Category;
import com.learningpurpose.examservice.repository.CategoryRepository;
import com.learningpurpose.examservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;


    @Override
    public Category addCategory(CategoryDto categoryDto) {
        Category category = Category.builder()
                .title(categoryDto.getTitle())
                .description(categoryDto.getDescription())
                .build();
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long id, CategoryDto categoryDto) {
        Category category = getCategory(id);
        category.setTitle(categoryDto.getTitle());
        category.setDescription(categoryDto.getDescription());
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    @Override
    public void deleteCategory(Long categoryId) {
        Category category = getCategory(categoryId);
        categoryRepository.delete(category);
    }
}