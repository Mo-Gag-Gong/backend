package com.mogacko.mogacko.repository;

import com.mogacko.mogacko.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByOauthId(String oauthId);
    boolean existsByEmail(String email);
}