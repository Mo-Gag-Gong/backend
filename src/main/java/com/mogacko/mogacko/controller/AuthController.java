package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.AuthResponse;
import com.mogacko.mogacko.dto.TokenRequest;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody TokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        if (response == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        User user = authService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        return ResponseEntity.ok(user);
    }
}