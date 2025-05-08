package com.mogacko.mogacko.repository;

import com.mogacko.mogacko.entity.Interest;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findByUser(User user);

    Optional<UserInterest> findByUserAndInterest(User user, Interest interest);

    @Query("SELECT ui.interest FROM UserInterest ui WHERE ui.user = :user")
    List<Interest> findUserInterests(User user);
}