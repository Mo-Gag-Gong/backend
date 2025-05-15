// src/main/java/com/mogacko/mogacko/controller/MeetingController.java
package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.*;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AuthService;
import com.mogacko.mogacko.service.MeetingService;
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
@RequestMapping("/api/groups/{groupId}/meetings")
@RequiredArgsConstructor
@Tag(name = "모임", description = "스터디 그룹 모임 관리 API")
public class MeetingController {

    private final MeetingService meetingService;
    private final AuthService authService;

    @Operation(summary = "모임 목록 조회", description = "스터디 그룹의 모임 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "모임 목록 조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<MeetingDto>> getMeetings(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId) {

        List<MeetingDto> meetings = meetingService.getMeetings(groupId);
        return ResponseEntity.ok(meetings);
    }

    @Operation(summary = "모임 상세 조회", description = "특정 모임의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "모임 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = MeetingDto.class))),
            @ApiResponse(responseCode = "404", description = "모임을 찾을 수 없음")
    })
    @GetMapping("/{meetingId}")
    public ResponseEntity<MeetingDto> getMeetingDetails(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "모임 ID") @PathVariable Long meetingId) {

        MeetingDto meeting = meetingService.getMeetingDetails(groupId, meetingId);
        if (meeting == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(meeting);
    }

    @Operation(summary = "모임 생성", description = "새로운 모임을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "모임 생성 성공",
                    content = @Content(schema = @Schema(implementation = MeetingDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "모임 생성 권한 없음")
    })
    @PostMapping
    public ResponseEntity<MeetingDto> createMeeting(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "모임 생성 정보") @RequestBody MeetingCreateRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        MeetingDto createdMeeting = meetingService.createMeeting(currentUser, groupId, request);
        if (createdMeeting == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(createdMeeting);
    }

    @Operation(summary = "모임 참가", description = "모임에 참가 신청을 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참가 신청 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "400", description = "참가 실패 (최대 인원 초과 등)")
    })
    @PostMapping("/{meetingId}/join")
    public ResponseEntity<?> joinMeeting(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "모임 ID") @PathVariable Long meetingId) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = meetingService.joinMeeting(currentUser, groupId, meetingId);
        if (!success) {
            return ResponseEntity.badRequest().body("Cannot join meeting");
        }

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "출석 체크", description = "모임에 출석 체크를 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "출석 체크 결과",
                    content = @Content(schema = @Schema(implementation = CheckInResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/{meetingId}/checkin")
    public ResponseEntity<CheckInResponse> checkIn(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "모임 ID") @PathVariable Long meetingId,
            @Parameter(description = "위치 정보") @RequestBody CheckInRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        CheckInResponse response = meetingService.checkIn(currentUser, groupId, meetingId, request);
        return ResponseEntity.ok(response);
    }
}