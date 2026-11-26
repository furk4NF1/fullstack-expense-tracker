package com.portfolio.expensetracker.service;

import com.portfolio.expensetracker.dto.CategoryResponse;
import com.portfolio.expensetracker.dto.CategoryRequest;
import com.portfolio.expensetracker.entity.Category;
import com.portfolio.expensetracker.entity.User;
import com.portfolio.expensetracker.exception.DuplicateResourceException;
import com.portfolio.expensetracker.exception.ResourceInUseException;
import com.portfolio.expensetracker.exception.ResourceNotFoundException;
import com.portfolio.expensetracker.repository.CategoryRepository;
import com.portfolio.expensetracker.repository.ExpenseRepository;
import com.portfolio.expensetracker.repository.MonthlyBudgetRepository;
import com.portfolio.expensetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, ExpenseRepository expenseRepository,
                           MonthlyBudgetRepository monthlyBudgetRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.monthlyBudgetRepository = monthlyBudgetRepository;
        this.userRepository = userRepository;
    }

    public List<CategoryResponse> getAll(Long ownerId) {
        return categoryRepository.findAllByOwner_IdOrderByNameAsc(ownerId).stream()
                .map(category -> new CategoryResponse(category.getId(), category.getName(), category.getColor()))
                .toList();
    }

    @Transactional
    public CategoryResponse create(Long ownerId, CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndOwner_Id(name, ownerId)) {
            throw new DuplicateResourceException("Bu isimde bir kategori zaten var.");
        }

        Category category = categoryRepository.save(new Category(name, request.color(), getOwner(ownerId)));
        return new CategoryResponse(category.getId(), category.getName(), category.getColor());
    }

    @Transactional
    public CategoryResponse update(Long ownerId, Long id, CategoryRequest request) {
        Category category = getCategory(id, ownerId);
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndOwner_IdAndIdNot(name, ownerId, id)) {
            throw new DuplicateResourceException("Bu isimde başka bir kategori zaten var.");
        }

        category.update(name, request.color());
        return new CategoryResponse(category.getId(), category.getName(), category.getColor());
    }

    @Transactional
    public void delete(Long ownerId, Long id) {
        Category category = getCategory(id, ownerId);
        if (expenseRepository.existsByCategory_IdAndCategory_Owner_Id(id, ownerId)
                || monthlyBudgetRepository.existsByCategory_IdAndCategory_Owner_Id(id, ownerId)) {
            throw new ResourceInUseException(
                    "Bu kategori kayıtlı bir harcamada veya bütçede kullanılıyor. Önce ilgili kayıtları ya da bütçeyi değiştir.");
        }
        categoryRepository.delete(category);
    }

    private Category getCategory(Long id, Long ownerId) {
        return categoryRepository.findByIdAndOwner_Id(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori bulunamadı."));
    }

    private User getOwner(Long ownerId) {
        return userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Hesap bulunamadı."));
    }
}
