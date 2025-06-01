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

    @Query(value = "SELECT sg.* FROM study_groups sg " +
            "JOIN interests i ON sg.interest_id = i.interest_id " +
            "WHERE sg.is_active = true AND i.interest_name = :category",
            nativeQuery = true)
    Page<StudyGroup> findActiveGroupsByInterestName(@Param("category") String category, Pageable pageable);


    // LOWER 함수 사용 방식 수정
    @Query("SELECT sg FROM StudyGroup sg WHERE sg.title LIKE %:keyword% OR sg.description LIKE %:keyword%")
    Page<StudyGroup> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 또는 대소문자 구분 없는 검색이 필요하다면 다음과 같이 CAST 추가
    /*
    @Query("SELECT sg FROM StudyGroup sg WHERE " +
           "LOWER(CAST(sg.title AS string)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(CAST(sg.description AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<StudyGroup> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    */
}