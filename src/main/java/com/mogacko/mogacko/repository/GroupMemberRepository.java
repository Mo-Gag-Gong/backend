package com.mogacko.mogacko.repository;

import com.mogacko.mogacko.entity.GroupMember;
import com.mogacko.mogacko.entity.StudyGroup;
import com.mogacko.mogacko.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroup(StudyGroup group);

    List<GroupMember> findByUser(User user);

    Optional<GroupMember> findByGroupAndUser(StudyGroup group, User user);

    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group = :group AND gm.status = 'ACTIVE'")
    int countActiveMembers(StudyGroup group);

    @Query("SELECT gm.group FROM GroupMember gm WHERE gm.user = :user AND gm.status = 'ACTIVE'")
    List<StudyGroup> findUserGroups(User user);

    List<GroupMember> findByGroupAndStatus(StudyGroup group, String status);
}