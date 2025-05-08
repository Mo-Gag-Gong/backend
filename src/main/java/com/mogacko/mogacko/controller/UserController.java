package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.InterestDto;
import com.mogacko.mogacko.dto.ProfileUpdateRequest;
import com.mogacko.mogacko.dto.UserProfileDto;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AuthService;
import com.mogacko.mogacko.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "유저", description = "유저 API")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

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

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(@RequestBody ProfileUpdateRequest request) {
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

    @GetMapping("/interests")
    public ResponseEntity<List<InterestDto>> getAllInterests() {
        List<InterestDto> interests = userService.getAllInterests();
        return ResponseEntity.ok(interests);
    }

    @GetMapping("/my-interests")
    public ResponseEntity<List<InterestDto>> getUserInterests() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<InterestDto> interests = userService.getUserInterests(currentUser);
        return ResponseEntity.ok(interests);
    }

    @PostMapping("/interests/{interestId}")
    public ResponseEntity<?> addInterest(@PathVariable Long interestId) {
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

    @DeleteMapping("/interests/{interestId}")
    public ResponseEntity<?> removeInterest(@PathVariable Long interestId) {
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