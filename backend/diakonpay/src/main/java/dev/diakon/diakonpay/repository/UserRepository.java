package dev.diakon.diakonpay.repository;

import dev.diakon.diakonpay.entity.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<@NonNull User, @NonNull Integer> {
    Optional<User> findByUserEmail(@NonNull String userEmail);
}
