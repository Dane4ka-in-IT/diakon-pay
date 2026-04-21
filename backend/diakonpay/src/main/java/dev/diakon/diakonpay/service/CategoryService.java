package dev.diakon.diakonpay.service;

import dev.diakon.diakonpay.dto.category.CategoryRequestDto;
import dev.diakon.diakonpay.dto.category.CategoryResponseDto;
import dev.diakon.diakonpay.entity.Category;
import dev.diakon.diakonpay.entity.User;
import dev.diakon.diakonpay.exception.AccessDeniedException;
import dev.diakon.diakonpay.exception.SystemObjectModificationException;
import dev.diakon.diakonpay.exception.UserNotFound;
import dev.diakon.diakonpay.repository.CategoryRepository;
import dev.diakon.diakonpay.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private static final Integer SYSTEM_USER_ID = 1;

    private Category getCategoryByCategoryId(UUID categoryId){
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

    }

    public List<CategoryResponseDto> getUserCategories(Integer userId) {
        return categoryRepository.findAllByUserIdOrUserId(userId, SYSTEM_USER_ID).stream()
                .map(cat -> new CategoryResponseDto(
                        cat.getId(),
                        cat.getUser().getId(),
                        cat.getNameCategory(),
                        cat.getCategoryDescription(),
                        cat.getType()
                )).toList();
    }

    @Transactional
    public UUID createCategory(Integer userId, CategoryRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound("Пользователь не найден"));

        Category category = new Category();
        category.setId(request.id() != null ? request.id() : UUID.randomUUID());
        category.setUser(user);
        category.setNameCategory(request.nameCategory());
        category.setCategoryDescription(request.categoryDescription());
        category.setType(request.type());

        categoryRepository.save(category);
        return category.getId();
    }

    @Transactional
    public void updateCategory(Integer userId, UUID categoryId, CategoryRequestDto request) {
        Category category = getCategoryByCategoryId(categoryId);
        validation(userId, category);

        category.setNameCategory(request.nameCategory());
        category.setCategoryDescription(request.categoryDescription());
        category.setType(request.type());

        categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Integer userId, UUID categoryId) {
        Category category = getCategoryByCategoryId(categoryId);
        validation(userId, category);
        categoryRepository.delete(category);
    }

    private void validation(Integer userId, Category category){
        if (SYSTEM_USER_ID.equals(category.getUser().getId())) {
            throw new SystemObjectModificationException("Нельзя изменять системные категории");
        }
        if (!userId.equals(category.getUser().getId())) {
            throw new AccessDeniedException("Доступ запрещен");
        }
    }
}