package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.AttendanceDto;
import com.mogacko.mogacko.dto.CheckInRequest;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AttendanceService;
import com.mogacko.mogacko.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/attendance")
@RequiredArgsConstructor
@Tag(name = "스터디 그룹 참가자", description = "스터디 그룹 출석 관리 API - 스터디 그룹 참가자의 출석을 관리합니다.")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AuthService authService;

    /**
     * 특정 스터디 그룹의 특정 날짜 출석 목록을 조회합니다.
     *
     * @param groupId 조회할 스터디 그룹 ID
     * @param date 조회할 날짜
     * @return 출석 정보 목록
     */
    @Operation(summary = "그룹 출석 목록 조회", description = "특정 스터디 그룹의 특정 날짜 출석 현황을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "출석 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "스터디 그룹을 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<List<AttendanceDto>> getGroupAttendance(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "조회할 날짜 (ISO 포맷: yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<AttendanceDto> attendances = attendanceService.getGroupAttendance(groupId, date);
        if (attendances == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(attendances);
    }

    /**
     * 스터디 그룹 세션에 체크인합니다.
     *
     * @param groupId 체크인할 스터디 그룹 ID
     * @param request 체크인 요청 정보 (세션 날짜)
     * @return 출석 정보
     */
    @Operation(summary = "출석 체크인", description = "스터디 그룹 세션에 체크인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "체크인 성공",
                    content = @Content(schema = @Schema(implementation = AttendanceDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "체크인 권한 없음")
    })
    @PostMapping("/check-in")
    public ResponseEntity<AttendanceDto> checkIn(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "체크인 요청 정보") @RequestBody CheckInRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        AttendanceDto attendance = attendanceService.checkIn(currentUser, groupId, request);
        if (attendance == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(attendance);
    }

    /**
     * 스터디 그룹 세션에서 체크아웃합니다.
     *
     * @param groupId 체크아웃할 스터디 그룹 ID
     * @param request 체크아웃 요청 정보 (세션 날짜)
     * @return 출석 정보
     */
    @Operation(summary = "출석 체크아웃", description = "스터디 그룹 세션에서 체크아웃합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "체크아웃 성공",
                    content = @Content(schema = @Schema(implementation = AttendanceDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "체크아웃 권한 없음 또는 체크인 기록 없음")
    })
    @PostMapping("/check-out")
    public ResponseEntity<AttendanceDto> checkOut(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Long groupId,
            @Parameter(description = "체크아웃 요청 정보") @RequestBody CheckInRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        AttendanceDto attendance = attendanceService.checkOut(currentUser, groupId, request);
        if (attendance == null) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(attendance);
    }
}