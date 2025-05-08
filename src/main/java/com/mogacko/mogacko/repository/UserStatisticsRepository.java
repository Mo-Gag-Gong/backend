package com.mogacko.mogacko.repository;

import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {
    Optional<UserStatistics> findByUser(User user);
}