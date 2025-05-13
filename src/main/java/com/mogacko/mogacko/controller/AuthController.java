package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.AuthResponse;
import com.mogacko.mogacko.dto.TokenRequest;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AuthService;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "사용자 인증 및 토큰 관리 API")
public class AuthController {

    private final AuthService authService;

    /**
     * Refresh Token으로 새로운 Access Token과 Refresh Token을 발급합니다.
     *
     * @param request 갱신할 Refresh Token 정보
     * @return 새로운 인증 토큰 정보
     */
    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token과 Refresh Token을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰")
    })
    @PostMapping("/token/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Parameter(description = "갱신할 Refresh Token 정보") @RequestBody TokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        if (response == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 인증된 사용자 정보를 조회합니다.
     *
     * @return 현재 인증된 사용자 정보
     */
    @Operation(summary = "현재 사용자 정보 조회", description = "현재 인증된 사용자 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        User user = authService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok(user);
    }
}