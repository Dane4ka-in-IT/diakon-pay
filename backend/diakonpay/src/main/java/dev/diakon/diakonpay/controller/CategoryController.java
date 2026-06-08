package dev.diakon.diakonpay.controller;

import dev.diakon.diakonpay.dto.category.CategoryRequestDto;
import dev.diakon.diakonpay.dto.category.CategoryResponseDto;
import dev.diakon.diakonpay.service.CategoryService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<@NonNull List<CategoryResponseDto>> getCategories(@AuthenticationPrincipal Integer userId) {
        return ResponseEntity.ok(categoryService.getUserCategories(userId));
    }

    @PostMapping
    public ResponseEntity<@NonNull Map<String, Object>> createCategory(
            @AuthenticationPrincipal Integer userId,
            @RequestBody CategoryRequestDto request) {
        UUID id = categoryService.createCategory(userId, request);
        return ResponseEntity.ok(Map.of(
                "message", "Категория успешно создана",
                "categoryId", id
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<@NonNull Map<String, String>> updateCategory(
            @AuthenticationPrincipal Integer userId,
            @PathVariable UUID id,
            @RequestBody CategoryRequestDto request) {
        categoryService.updateCategory(userId, id, request);
        return ResponseEntity.ok(Map.of("message", "Категория успешно обновлена"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<@NonNull Map<String, Object>> deleteCategory(
            @AuthenticationPrincipal Integer userId,
            @PathVariable UUID id) {
        categoryService.deleteCategory(userId, id);
        return ResponseEntity.ok(Map.of(
                "id", id,
                "message", "Категория удалена"
        ));
    }
}