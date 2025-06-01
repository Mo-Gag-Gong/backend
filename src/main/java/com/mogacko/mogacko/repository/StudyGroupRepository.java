package com.mogacko.mogacko.repository;

import com.mogacko.mogacko.entity.StudyGroup;
import com.mogacko.mogacko.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    List<StudyGroup> findByCreator(User creator);

    @Query("SELECT sg FROM StudyGroup sg WHERE sg.isActive = true")
    Page<StudyGroup> findActiveGroups(Pageable pageable);

    @Query("SELECT sg FROM StudyGroup sg " +
            "JOIN sg.interest i " +
            "WHERE sg.isActive = true AND i.interestName = :category")
    Page<StudyGroup> findActiveGroupsByInterestName(@Param("category") String category, Pageable pageable);


    @Query("SELECT sg FROM StudyGroup sg " +
            "WHERE (LOWER(sg.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(sg.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND sg.isActive = true")
    Page<StudyGroup> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}