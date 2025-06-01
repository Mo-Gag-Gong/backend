package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.AuthResponse;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserProfile;
import com.mogacko.mogacko.entity.UserStatistics;
import com.mogacko.mogacko.repository.UserProfileRepository;
import com.mogacko.mogacko.repository.UserRepository;
import com.mogacko.mogacko.repository.UserStatisticsRepository;
import com.mogacko.mogacko.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * 테스트용 토큰 발급 컨트롤러
 * ⚠️ 프로덕션 환경에서는 반드시 제거해야 합니다!
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "테스트", description = "테스트용 API - 프로덕션에서는 제거")
public class TestTokenController {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserStatisticsRepository userStatisticsRepository;

    /**
     * 테스트용 사용자와 토큰을 생성합니다.
     *
     * @param email 테스트 사용자 이메일 (기본값: test@example.com)
     * @return 생성된 사용자 정보와 토큰
     */
    @Operation(summary = "테스트용 토큰 생성",
            description = "테스트용 사용자를 생성하고 JWT 토큰을 발급합니다. 프로덕션에서는 사용하지 마세요!")
    @PostMapping("/create-token")
    public ResponseEntity<Map<String, Object>> createTestToken(
            @RequestParam(defaultValue = "test@example.com") String email) {

        // 기존 사용자 확인
        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            // 새 테스트 사용자 생성
            user = User.builder()
                    .email(email)
                    .profileImage("https://via.placeholder.com/150")
                    .oauthId("test_oauth_id")
                    .provider("test")
                    .role("ROLE_USER")
                    .build();

            user = userRepository.save(user);

            // 프로필 생성
            UserProfile profile = UserProfile.builder()
                    .user(user)
                    .name("테스트 사용자")
                    .gender("M")
                    .phoneNumber("010-1234-5678")
                    .birthYear(1990)
                    .locationName("서울시 강남구")
                    .onboardingCompleted(true)
                    .build();

            userProfileRepository.save(profile);

            // 통계 생성
            UserStatistics statistics = UserStatistics.builder()
                    .user(user)
                    .groupParticipationCount(0)
                    .attendanceRate(0.0)
                    .totalMeetings(0)
                    .lastUpdated(LocalDateTime.now())
                    .build();

            userStatisticsRepository.save(statistics);
        }

        // 토큰 생성
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        return ResponseEntity.ok(Map.of(
                "message", "테스트용 토큰이 생성되었습니다.",
                "userId", user.getUserId(),
                "email", user.getEmail(),
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "tokenType", "Bearer",
                "warning", "⚠️ 이는 테스트용 토큰입니다. 프로덕션에서는 사용하지 마세요!"
        ));
    }

    /**
     * 기존 사용자 ID로 토큰을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return 토큰 정보
     */
    @Operation(summary = "사용자 ID로 토큰 생성",
            description = "기존 사용자 ID로 JWT 토큰을 발급합니다.")
    @PostMapping("/token-by-userid/{userId}")
    public ResponseEntity<AuthResponse> createTokenByUserId(@PathVariable Long userId) {

        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, user.getUserId()));
    }

    /**
     * 모든 사용자 목록을 조회합니다.
     *
     * @return 사용자 목록
     */
    @Operation(summary = "전체 사용자 목록 조회",
            description = "테스트용으로 전체 사용자 목록을 조회합니다.")
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}