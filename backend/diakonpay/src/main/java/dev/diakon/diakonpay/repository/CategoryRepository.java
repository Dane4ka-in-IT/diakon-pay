package dev.diakon.diakonpay.repository;

import dev.diakon.diakonpay.entity.Category;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<@NonNull Category, @NonNull UUID> {
    List<Category> findAllByUserIdOrUserId(Integer userId, Integer systemUserId);
}