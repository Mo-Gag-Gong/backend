package com.mogacko.mogacko.repository;

import com.mogacko.mogacko.entity.GroupGoal;
import com.mogacko.mogacko.entity.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupGoalRepository extends JpaRepository<GroupGoal, Long> {
    List<GroupGoal> findByGroup(StudyGroup group);

    List<GroupGoal> findByGroupOrderByEndDateDesc(StudyGroup group);
}