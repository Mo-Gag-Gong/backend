package com.mogacko.mogacko.repository;

import com.mogacko.mogacko.entity.GroupGoal;
import com.mogacko.mogacko.entity.GroupGoalDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupGoalDetailRepository extends JpaRepository<GroupGoalDetail, Long> {
    List<GroupGoalDetail> findByGoal(GroupGoal goal);

    List<GroupGoalDetail> findByGoalAndIsCompleted(GroupGoal goal, Boolean isCompleted);
}