// src/main/java/com/mogacko/mogacko/repository/UserProfileRepository.java
package com.mogacko.mogacko.repository;

import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser(User user);
    Optional<UserProfile> findByUserUserId(Long userId);
}