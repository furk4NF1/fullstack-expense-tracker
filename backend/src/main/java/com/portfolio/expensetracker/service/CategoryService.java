package com.portfolio.expensetracker.service;

import com.portfolio.expensetracker.dto.CategoryResponse;
import com.portfolio.expensetracker.dto.CategoryRequest;
import com.portfolio.expensetracker.entity.Category;
import com.portfolio.expensetracker.exception.DuplicateResourceException;
import com.portfolio.expensetracker.exception.ResourceInUseException;
import com.portfolio.expensetracker.exception.ResourceNotFoundException;
import com.portfolio.expensetracker.repository.CategoryRepository;
import com.portfolio.expensetracker.repository.ExpenseRepository;
import com.portfolio.expensetracker.repository.MonthlyBudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final MonthlyBudgetRepository monthlyBudgetRepository;

    public CategoryService(CategoryRepository categoryRepository, ExpenseRepository expenseRepository,
                           MonthlyBudgetRepository monthlyBudgetRepository) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.monthlyBudgetRepository = monthlyBudgetRepository;
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

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getCategory(id);
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Bu isimde başka bir kategori zaten var.");
        }

        category.update(name, request.color());
        return new CategoryResponse(category.getId(), category.getName(), category.getColor());
    }

    @Transactional
    public void delete(Long id) {
        Category category = getCategory(id);
        if (expenseRepository.existsByCategory_Id(id) || monthlyBudgetRepository.existsByCategory_Id(id)) {
            throw new ResourceInUseException(
                    "Bu kategori kayıtlı bir harcamada veya bütçede kullanılıyor. Önce ilgili kayıtları ya da bütçeyi değiştir.");
        }
        categoryRepository.delete(category);
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı."));
    }
}

