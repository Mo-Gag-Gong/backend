package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.GroupNoticeCreateRequest;
import com.mogacko.mogacko.dto.GroupNoticeDto;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AuthService;
import com.mogacko.mogacko.service.GroupNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups/{groupId}/notices")
@RequiredArgsConstructor
@Tag(name = "스터디 그룹 공지", description = "스터디 그룹 공지사항 관리 API - 그룹 내 공지사항을 관리합니다.")
public class GroupNoticeController {

    private final GroupNoticeService noticeService;
    private final AuthService authService;

    /**
     * 스터디 그룹의 공지사항 목록을 페이지 단위로 조회합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지 크기
     * @return 공지사항 목록
     */
    @Operation(summary = "그룹 공지사항 목록 조회", description = "스터디 그룹의 공지사항 목록을 페이지 단위로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지사항 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "스터디 그룹을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<Page<GroupNoticeDto>> getGroupNotices(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "페이지 번호 (1부터 시작)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {

        Page<GroupNoticeDto> notices = noticeService.getGroupNotices(groupId, page, size);
        if (notices == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(notices);
    }

    /**
     * 특정 공지사항의 세부 내용을 조회합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @param noticeId 공지사항 ID
     * @return 공지사항 세부 내용
     */
    @Operation(summary = "공지사항 세부 내용 조회", description = "특정 공지사항의 세부 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지사항 조회 성공",
                    content = @Content(schema = @Schema(implementation = GroupNoticeDto.class))),
            @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    })
    @GetMapping("/{noticeId}")
    public ResponseEntity<GroupNoticeDto> getNoticeDetails(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "공지사항 ID") @PathVariable Long noticeId) {

        GroupNoticeDto notice = noticeService.getNoticeDetails(groupId, noticeId);
        if (notice == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(notice);
    }

    /**
     * 새로운 공지사항을 작성합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @param request 공지사항 작성 정보 (제목, 내용)
     * @return 생성된 공지사항 정보
     */
    @Operation(summary = "공지사항 작성", description = "스터디 그룹에 새로운 공지사항을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지사항 작성 성공",
                    content = @Content(schema = @Schema(implementation = GroupNoticeDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "공지사항 작성 권한 없음")
    })
    @PostMapping
    public ResponseEntity<GroupNoticeDto> createNotice(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "공지사항 작성 정보") @RequestBody GroupNoticeCreateRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        GroupNoticeDto createdNotice = noticeService.createNotice(currentUser, groupId, request);
        if (createdNotice == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(createdNotice);
    }

    /**
     * 기존 공지사항을 수정합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @param noticeId 수정할 공지사항 ID
     * @param request 공지사항 수정 정보 (제목, 내용)
     * @return 수정된 공지사항 정보
     */
    @Operation(summary = "공지사항 수정", description = "기존 공지사항의 내용을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지사항 수정 성공",
                    content = @Content(schema = @Schema(implementation = GroupNoticeDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "공지사항 수정 권한 없음")
    })
    @PutMapping("/{noticeId}")
    public ResponseEntity<GroupNoticeDto> updateNotice(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "공지사항 ID") @PathVariable Long noticeId,
            @Parameter(description = "공지사항 수정 정보") @RequestBody GroupNoticeCreateRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        GroupNoticeDto updatedNotice = noticeService.updateNotice(currentUser, groupId, noticeId, request);
        if (updatedNotice == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(updatedNotice);
    }

    /**
     * 공지사항을 삭제합니다.
     *
     * @param groupId 스터디 그룹 ID
     * @param noticeId 삭제할 공지사항 ID
     * @return 삭제 성공 여부
     */
    @Operation(summary = "공지사항 삭제", description = "스터디 그룹의 공지사항을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지사항 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "공지사항 삭제 권한 없음")
    })
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<?> deleteNotice(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "공지사항 ID") @PathVariable Long noticeId) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = noticeService.deleteNotice(currentUser, groupId, noticeId);
        if (!success) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok().build();
    }
}