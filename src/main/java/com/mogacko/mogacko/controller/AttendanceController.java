package com.mogacko.mogacko.controller;

import com.mogacko.mogacko.dto.AttendanceDto;
import com.mogacko.mogacko.dto.CheckInRequest;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.service.AttendanceService;
import com.mogacko.mogacko.service.AuthService;
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
@Tag(name = "스터디 그룹 참가자", description = "스터디 그룹 참가자 API")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<AttendanceDto>> getGroupAttendance(
            @PathVariable Long groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<AttendanceDto> attendances = attendanceService.getGroupAttendance(groupId, date);
        if (attendances == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(attendances);
    }

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceDto> checkIn(
            @PathVariable Long groupId,
            @RequestBody CheckInRequest request) {

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

    @PostMapping("/check-out")
    public ResponseEntity<AttendanceDto> checkOut(
            @PathVariable Long groupId,
            @RequestBody CheckInRequest request) {

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