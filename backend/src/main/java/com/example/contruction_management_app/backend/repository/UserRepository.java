package com.example.contruction_management_app.backend.repository;

import com.example.contruction_management_app.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findFirstByEmailStartingWithIgnoreCase(String prefix);
    boolean existsByEmail(String email);
}
