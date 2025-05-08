package com.mogacko.mogacko.repository;

import com.mogacko.mogacko.entity.GroupNotice;
import com.mogacko.mogacko.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupNoticeRepository extends JpaRepository<GroupNotice, Long> {
    Page<GroupNotice> findByGroupOrderByCreatedAtDesc(StudyGroup group, Pageable pageable);
}