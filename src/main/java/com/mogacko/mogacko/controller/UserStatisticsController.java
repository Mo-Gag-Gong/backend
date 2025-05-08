package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.UserStatisticsDto;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AuthService;
import com.mogacko.mogacko.service.UserStatisticsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/statistics")
@RequiredArgsConstructor
@Tag(name = "유저 통계", description = "유저 통계 API")
public class UserStatisticsController {

    private final UserStatisticsService statisticsService;
    private final AuthService authService;

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