package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.InterestDto;
import com.mogacko.mogacko.dto.ProfileUpdateRequest;
import com.mogacko.mogacko.dto.UserProfileDto;
import com.mogacko.mogacko.dto.UserProfileWithStatsDto;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserProfile;
import com.mogacko.mogacko.repository.UserProfileRepository;
import com.mogacko.mogacko.service.AuthService;
import com.mogacko.mogacko.service.UserService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "유저", description = "사용자 프로필 및 관심사 관리 API - 사용자 정보와 관심사를 관리합니다.")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final UserProfileRepository userProfileRepository;

    /**
     * 현재 사용자의 프로필 정보를 조회합니다.
     *
     * @return 사용자 프로필 정보
     */
    @Operation(summary = "사용자 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "프로필 정보를 찾을 수 없음")
    })
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getUserProfile() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        UserProfileDto profileDto = userService.getUserProfile(currentUser);
        if (profileDto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(profileDto);
    }

    /**
     * 사용자 프로필을 조회합니다. 공개 프로필 정보와 통계 정보를 함께 반환합니다.
     * 자신의 프로필인 경우 개인정보를 포함한 전체 정보를 반환합니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 프로필 및 통계 정보
     */
    @Operation(summary = "사용자 프로필 및 통계 조회", description = "사용자의 공개 프로필과 통계 정보를 조회합니다. 자신의 프로필은 개인정보를 포함한 전체 정보를 조회 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileWithStatsDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileWithStatsDto> getUserProfile(
            @Parameter(description = "조회할 사용자 ID") @PathVariable Long userId) {

        User currentUser = authService.getCurrentUser();
        boolean isOwnProfile = currentUser != null && currentUser.getUserId().equals(userId);

        UserProfileWithStatsDto profileWithStats = userService.getUserProfileWithStats(userId, isOwnProfile);

        if (profileWithStats == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(profileWithStats);
    }

    /**
     * 사용자의 온보딩 상태를 확인합니다.
     *
     * @return 온보딩 상태 정보
     */
    @Operation(summary = "사용자 온보딩 상태(프로필 작성 상태) 확인", description = "현재 로그인한 사용자의 온보딩 완료 여부를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "온보딩 상태 확인 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/onboarding-status")
    public ResponseEntity<Map<String, Object>> getOnboardingStatus() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<UserProfile> profileOpt = userProfileRepository.findByUser(currentUser);
        boolean onboardingCompleted = profileOpt.isPresent() && profileOpt.get().getOnboardingCompleted();

        Map<String, Object> response = new HashMap<>();
        response.put("onboardingCompleted", onboardingCompleted);
        response.put("role", currentUser.getRole());

        return ResponseEntity.ok(response);
    }

    /**
     * 마이페이지 - 현재 사용자의 프로필 정보를 수정합니다.
     *
     * @param request 프로필 수정 정보 (이름, 성별, 전화번호, 출생년도, 관심사 ID 목록)
     * @return 수정된 프로필 정보
     */
    @Operation(summary = "사용자 프로필 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "프로필 정보를 찾을 수 없음")
    })
    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(
            @Parameter(description = "프로필 수정 정보") @RequestBody ProfileUpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        UserProfileDto updatedProfile = userService.updateProfile(currentUser, request);
        if (updatedProfile == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * 시스템에 등록된 모든 관심사 목록을 조회합니다.
     *
     * @return 전체 관심사 목록
     */
    @Operation(summary = "전체 관심사 목록 조회", description = "시스템에 등록된 모든 활성 상태의 관심사 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관심사 목록 조회 성공")
    })
    @GetMapping("/interests")
    public ResponseEntity<List<InterestDto>> getAllInterests() {
        List<InterestDto> interests = userService.getAllInterests();
        return ResponseEntity.ok(interests);
    }

    /**
     * 현재 사용자가 등록한 관심사 목록을 조회합니다.
     *
     * @return 사용자의 관심사 목록
     */
    @Operation(summary = "사용자 관심사 목록 조회", description = "현재 로그인한 사용자가 등록한 관심사 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 관심사 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/my-interests")
    public ResponseEntity<List<InterestDto>> getUserInterests() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<InterestDto> interests = userService.getUserInterests(currentUser);
        return ResponseEntity.ok(interests);
    }

    /**
     * 사용자 프로필에 새 관심사를 추가합니다.
     *
     * @param interestId 추가할 관심사 ID
     * @return 추가 성공 여부
     */
    @Operation(summary = "관심사 추가", description = "현재 로그인한 사용자의 프로필에 새 관심사를 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관심사 추가 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "관심사를 찾을 수 없음")
    })
    @PostMapping("/interests/{interestId}")
    public ResponseEntity<?> addInterest(
            @Parameter(description = "추가할 관심사 ID") @PathVariable Long interestId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = userService.addInterest(currentUser, interestId);
        if (!success) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 프로필에서 관심사를 제거합니다.
     *
     * @param interestId 제거할 관심사 ID
     * @return 제거 성공 여부
     */
    @Operation(summary = "관심사 제거", description = "현재 로그인한 사용자의 프로필에서 관심사를 제거합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관심사 제거 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "관심사를 찾을 수 없음")
    })
    @DeleteMapping("/interests/{interestId}")
    public ResponseEntity<?> removeInterest(
            @Parameter(description = "제거할 관심사 ID") @PathVariable Long interestId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = userService.removeInterest(currentUser, interestId);
        if (!success) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}