package com.portfolio.expensetracker.service;

import com.portfolio.expensetracker.dto.CategoryResponse;
import com.portfolio.expensetracker.dto.CategoryRequest;
import com.portfolio.expensetracker.entity.Category;
import com.portfolio.expensetracker.exception.DuplicateResourceException;
import com.portfolio.expensetracker.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(category -> new CategoryResponse(category.getId(), category.getName(), category.getColor()))
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Bu isimde bir kategori zaten var.");
        }

        Category category = categoryRepository.save(new Category(name, request.color()));
        return new CategoryResponse(category.getId(), category.getName(), category.getColor());
    }
}

