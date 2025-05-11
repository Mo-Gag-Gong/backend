package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.UserStatisticsDto;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AuthService;
import com.mogacko.mogacko.service.UserStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/statistics")
@RequiredArgsConstructor
@Tag(name = "유저 통계", description = "사용자 활동 통계 API - 사용자의 참여, 출석, 스터디 세션 등의 통계 정보를 제공합니다.")
public class UserStatisticsController {

    private final UserStatisticsService statisticsService;
    private final AuthService authService;

    /**
     * 현재 사용자의 통계 정보를 조회합니다.
     * 통계 정보가 없으면 자동으로 업데이트하여 생성합니다.
     *
     * @return 사용자 통계 정보
     */
    @Operation(summary = "사용자 통계 정보 조회", description = "현재 로그인한 사용자의 활동 통계 정보를 조회합니다. 통계 정보가 없으면 자동으로 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "통계 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserStatisticsDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping
    public ResponseEntity<UserStatisticsDto> getUserStatistics() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        UserStatisticsDto statistics = statisticsService.getUserStatistics(currentUser);
        if (statistics == null) {
            // 통계가 없으면 업데이트하여 생성
            statistics = statisticsService.updateUserStatistics(currentUser);
        }

        return ResponseEntity.ok(statistics);
    }

    /**
     * 현재 사용자의 통계 정보를 최신 데이터로 갱신합니다.
     *
     * @return 갱신된 사용자 통계 정보
     */
    @Operation(summary = "사용자 통계 정보 갱신", description = "현재 로그인한 사용자의 활동 통계 정보를 최신 데이터로 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "통계 정보 갱신 성공",
                    content = @Content(schema = @Schema(implementation = UserStatisticsDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/refresh")
    public ResponseEntity<UserStatisticsDto> refreshUserStatistics() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        UserStatisticsDto statistics = statisticsService.updateUserStatistics(currentUser);
        return ResponseEntity.ok(statistics);
    }
}