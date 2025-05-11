package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.GroupCreateRequest;
import com.mogacko.mogacko.dto.GroupMemberDto;
import com.mogacko.mogacko.dto.StudyGroupDto;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AuthService;
import com.mogacko.mogacko.service.StudyGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "스터디 그룹", description = "스터디 그룹 관리 API - 스터디 그룹 생성, 조회, 수정, 삭제 및 가입/탈퇴 기능을 제공합니다.")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;
    private final AuthService authService;

    /**
     * 모든 활성 스터디 그룹 목록을 페이지 단위로 조회합니다.
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 스터디 그룹 목록
     */
    @Operation(summary = "전체 스터디 그룹 목록 조회", description = "모든 활성 상태의 스터디 그룹 목록을 페이지 단위로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 목록 조회 성공")
    })
    @GetMapping
    public ResponseEntity<Page<StudyGroupDto>> getAllGroups(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {

        Page<StudyGroupDto> groups = studyGroupService.getAllGroups(page, size);
        return ResponseEntity.ok(groups);
    }

    /**
     * 특정 카테고리의 스터디 그룹 목록을 페이지 단위로 조회합니다.
     *
     * @param category 스터디 그룹 카테고리
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 카테고리별 스터디 그룹 목록
     */
    @Operation(summary = "카테고리별 스터디 그룹 목록 조회", description = "특정 카테고리의 활성 상태 스터디 그룹 목록을 페이지 단위로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리별 그룹 목록 조회 성공")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<StudyGroupDto>> getGroupsByCategory(
            @Parameter(description = "스터디 그룹 카테고리") @PathVariable String category,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {

        Page<StudyGroupDto> groups = studyGroupService.getGroupsByCategory(category, page, size);
        return ResponseEntity.ok(groups);
    }

    /**
     * 키워드로 스터디 그룹을 검색합니다.
     *
     * @param keyword 검색 키워드
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 검색 결과 스터디 그룹 목록
     */
    @Operation(summary = "스터디 그룹 검색", description = "제목 또는 설명에 키워드가 포함된 스터디 그룹을 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 검색 성공")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<StudyGroupDto>> searchGroups(
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {

        Page<StudyGroupDto> groups = studyGroupService.searchGroups(keyword, page, size);
        return ResponseEntity.ok(groups);
    }

    /**
     * 현재 사용자가 가입한 스터디 그룹 목록을 조회합니다.
     *
     * @return 사용자의 스터디 그룹 목록
     */
    @Operation(summary = "내 스터디 그룹 목록 조회", description = "현재 로그인한 사용자가 가입한 모든 스터디 그룹 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내 그룹 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/my-groups")
    public ResponseEntity<List<StudyGroupDto>> getMyGroups() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<StudyGroupDto> groups = studyGroupService.getMyGroups(currentUser);
        return ResponseEntity.ok(groups);
    }

    /**
     * 특정 스터디 그룹의 세부 정보를 조회합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @return 스터디 그룹 세부 정보
     */
    @Operation(summary = "스터디 그룹 상세 정보 조회", description = "특정 스터디 그룹의 세부 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 상세 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = StudyGroupDto.class))),
            @ApiResponse(responseCode = "404", description = "스터디 그룹을 찾을 수 없음")
    })
    @GetMapping("/{groupId}")
    public ResponseEntity<StudyGroupDto> getGroupDetails(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId) {
        StudyGroupDto group = studyGroupService.getGroupDetails(groupId);
        if (group == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(group);
    }

    /**
     * 새로운 스터디 그룹을 생성합니다.
     *
     * @param request 그룹 생성 정보 (제목, 카테고리, 설명, 위치, 일정, 최대 인원 등)
     * @return 생성된 스터디 그룹 정보
     */
    @Operation(summary = "스터디 그룹 생성", description = "새로운 스터디 그룹을 생성하고 생성자를 자동으로 멤버로 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 생성 성공",
                    content = @Content(schema = @Schema(implementation = StudyGroupDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping
    public ResponseEntity<StudyGroupDto> createGroup(
            @Parameter(description = "그룹 생성 정보") @RequestBody GroupCreateRequest request) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        StudyGroupDto createdGroup = studyGroupService.createGroup(currentUser, request);
        return ResponseEntity.ok(createdGroup);
    }

    /**
     * 기존 스터디 그룹 정보를 수정합니다.
     *
     * @param groupId 수정할 스터디 그룹 ID
     * @param request 그룹 수정 정보 (제목, 카테고리, 설명, 위치, 일정, 최대 인원 등)
     * @return 수정된 스터디 그룹 정보
     */
    @Operation(summary = "스터디 그룹 정보 수정", description = "기존 스터디 그룹의 정보를 수정합니다. 그룹 생성자만 수정 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 정보 수정 성공",
                    content = @Content(schema = @Schema(implementation = StudyGroupDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "그룹 수정 권한 없음")
    })
    @PutMapping("/{groupId}")
    public ResponseEntity<StudyGroupDto> updateGroup(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "그룹 수정 정보") @RequestBody GroupCreateRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        StudyGroupDto updatedGroup = studyGroupService.updateGroup(currentUser, groupId, request);
        if (updatedGroup == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(updatedGroup);
    }

    /**
     * 스터디 그룹을 비활성화(삭제)합니다.
     *
     * @param groupId 비활성화할 스터디 그룹 ID
     * @return 비활성화 성공 여부
     */
    @Operation(summary = "스터디 그룹 비활성화", description = "스터디 그룹을 비활성화(논리적 삭제) 처리합니다. 그룹 생성자만 비활성화 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 비활성화 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "그룹 비활성화 권한 없음")
    })
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deactivateGroup(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = studyGroupService.deactivateGroup(currentUser, groupId);
        if (!success) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 특정 스터디 그룹의 멤버 목록을 조회합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @return 그룹 멤버 목록
     */
    @Operation(summary = "스터디 그룹 멤버 목록 조회", description = "특정 스터디 그룹의 모든 멤버 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 멤버 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "스터디 그룹을 찾을 수 없음")
    })
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberDto>> getGroupMembers(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId) {
        List<GroupMemberDto> members = studyGroupService.getGroupMembers(groupId);
        if (members == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(members);
    }

    /**
     * 스터디 그룹에 가입합니다.
     *
     * @param groupId 가입할 스터디 그룹 ID
     * @return 가입 성공 여부
     */
    @Operation(summary = "스터디 그룹 가입", description = "현재 로그인한 사용자를 스터디 그룹에 멤버로 가입시킵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 가입 성공"),
            @ApiResponse(responseCode = "400", description = "그룹 가입 실패 (최대 인원 초과 등)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinGroup(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = studyGroupService.joinGroup(currentUser, groupId);
        if (!success) {
            return ResponseEntity.badRequest().body("Cannot join group");
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 스터디 그룹에서 탈퇴합니다.
     *
     * @param groupId 탈퇴할 스터디 그룹 ID
     * @return 탈퇴 성공 여부
     */
    @Operation(summary = "스터디 그룹 탈퇴", description = "현재 로그인한 사용자를 스터디 그룹에서 탈퇴시킵니다. 그룹 생성자는 탈퇴할 수 없습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "그룹 탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = "그룹 탈퇴 실패 (그룹 생성자는 탈퇴 불가)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = studyGroupService.leaveGroup(currentUser, groupId);
        if (!success) {
            return ResponseEntity.badRequest().body("Cannot leave group");
        }

        return ResponseEntity.ok().build();
    }
}
