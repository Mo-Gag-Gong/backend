package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.GroupGoalCreateRequest;
import com.mogacko.mogacko.dto.GroupGoalDto;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AuthService;
import com.mogacko.mogacko.service.GroupGoalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/goals")
@RequiredArgsConstructor
@Tag(name = "스터디 그룹 목표", description = "스터디 그룹 목표 API")
public class GroupGoalController {

    private final GroupGoalService goalService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<GroupGoalDto>> getGroupGoals(@PathVariable Long groupId) {
        List<GroupGoalDto> goals = goalService.getGroupGoals(groupId);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<GroupGoalDto> getGoalDetails(
            @PathVariable Long groupId,
            @PathVariable Long goalId) {

        GroupGoalDto goal = goalService.getGoalDetails(groupId, goalId);
        if (goal == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(goal);
    }

    @PostMapping
    public ResponseEntity<GroupGoalDto> createGoal(
            @PathVariable Long groupId,
            @RequestBody GroupGoalCreateRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        GroupGoalDto createdGoal = goalService.createGoal(currentUser, groupId, request);
        if (createdGoal == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(createdGoal);
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<GroupGoalDto> updateGoal(
            @PathVariable Long groupId,
            @PathVariable Long goalId,
            @RequestBody GroupGoalCreateRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        GroupGoalDto updatedGoal = goalService.updateGoal(currentUser, groupId, goalId, request);
        if (updatedGoal == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(updatedGoal);
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<?> deleteGoal(
            @PathVariable Long groupId,
            @PathVariable Long goalId) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = goalService.deleteGoal(currentUser, groupId, goalId);
        if (!success) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{goalId}/details/{detailId}/toggle")
    public ResponseEntity<?> toggleGoalDetailCompletion(
            @PathVariable Long groupId,
            @PathVariable Long goalId,
            @PathVariable Long detailId) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = goalService.toggleGoalDetailCompletion(currentUser, groupId, goalId, detailId);
        if (!success) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok().build();
    }
}