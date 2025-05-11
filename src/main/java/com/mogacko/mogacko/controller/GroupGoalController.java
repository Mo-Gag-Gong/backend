package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.GroupGoalCreateRequest;
import com.mogacko.mogacko.dto.GroupGoalDto;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AuthService;
import com.mogacko.mogacko.service.GroupGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/goals")
@RequiredArgsConstructor
@Tag(name = "스터디 그룹 목표", description = "스터디 그룹 목표 관리 API - 그룹 내 학습 목표와 달성 현황을 관리합니다.")
public class GroupGoalController {

    private final GroupGoalService goalService;
    private final AuthService authService;

    /**
     * 스터디 그룹의 목표 목록을 조회합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @return 목표 목록
     */
    @Operation(summary = "그룹 목표 목록 조회", description = "스터디 그룹의 모든 학습 목표 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "목표 목록 조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<GroupGoalDto>> getGroupGoals(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId) {
        List<GroupGoalDto> goals = goalService.getGroupGoals(groupId);
        return ResponseEntity.ok(goals);
    }

    /**
     * 특정 목표의 세부 정보를 조회합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @param goalId 목표 ID
     * @return 목표 세부 정보
     */
    @Operation(summary = "목표 세부 정보 조회", description = "특정 학습 목표의 세부 정보와 세부 목표 항목을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "목표 세부 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = GroupGoalDto.class))),
            @ApiResponse(responseCode = "404", description = "목표를 찾을 수 없음")
    })
    @GetMapping("/{goalId}")
    public ResponseEntity<GroupGoalDto> getGoalDetails(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "목표 ID") @PathVariable Long goalId) {

        GroupGoalDto goal = goalService.getGoalDetails(groupId, goalId);
        if (goal == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(goal);
    }

    /**
     * 새로운 학습 목표를 생성합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @param request 목표 생성 정보 (제목, 포인트 가치, 종료일, 세부 목표)
     * @return 생성된 목표 정보
     */
    @Operation(summary = "학습 목표 생성", description = "새로운 학습 목표와 세부 목표 항목을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "목표 생성 성공",
                    content = @Content(schema = @Schema(implementation = GroupGoalDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "목표 생성 권한 없음")
    })
    @PostMapping
    public ResponseEntity<GroupGoalDto> createGoal(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "목표 생성 정보") @RequestBody GroupGoalCreateRequest request) {

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

    /**
     * 기존 학습 목표를 수정합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @param goalId 수정할 목표 ID
     * @param request 목표 수정 정보 (제목, 포인트 가치, 종료일, 세부 목표)
     * @return 수정된 목표 정보
     */
    @Operation(summary = "학습 목표 수정", description = "기존 학습 목표와 세부 목표 항목을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "목표 수정 성공",
                    content = @Content(schema = @Schema(implementation = GroupGoalDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "목표 수정 권한 없음")
    })
    @PutMapping("/{goalId}")
    public ResponseEntity<GroupGoalDto> updateGoal(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "목표 ID") @PathVariable Long goalId,
            @Parameter(description = "목표 수정 정보") @RequestBody GroupGoalCreateRequest request) {

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

    /**
     * 학습 목표를 삭제합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @param goalId 삭제할 목표 ID
     * @return 삭제 성공 여부
     */
    @Operation(summary = "학습 목표 삭제", description = "학습 목표와 관련된 세부 목표 항목을 모두 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "목표 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "목표 삭제 권한 없음")
    })
    @DeleteMapping("/{goalId}")
    public ResponseEntity<?> deleteGoal(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "목표 ID") @PathVariable Long goalId) {

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

    /**
     * 세부 목표 항목의 완료 상태를 토글합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @param goalId 목표 ID
     * @param detailId 세부 목표 항목 ID
     * @return 토글 성공 여부
     */
    @Operation(summary = "세부 목표 완료 상태 토글", description = "세부 목표 항목의 완료 상태를 변경합니다 (완료 <-> 미완료).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 토글 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "상태 토글 권한 없음")
    })
    @PostMapping("/{goalId}/details/{detailId}/toggle")
    public ResponseEntity<?> toggleGoalDetailCompletion(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "목표 ID") @PathVariable Long goalId,
            @Parameter(description = "세부 목표 항목 ID") @PathVariable Long detailId) {

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
