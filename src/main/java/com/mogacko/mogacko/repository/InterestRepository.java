package com.mogacko.mogacko.repository;

import com.mogacko.mogacko.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Long> {
    Optional<Interest> findByInterestName(String interestName);

    List<Interest> findByIsActiveTrue();
}