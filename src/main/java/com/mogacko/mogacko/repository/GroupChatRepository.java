// src/main/java/com/mogacko/mogacko/repository/GroupChatRepository.java
package com.mogacko.mogacko.repository;

import com.mogacko.mogacko.entity.GroupChat;
import com.mogacko.mogacko.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    Page<GroupChat> findByGroupOrderBySentAtDesc(StudyGroup group, Pageable pageable);
}