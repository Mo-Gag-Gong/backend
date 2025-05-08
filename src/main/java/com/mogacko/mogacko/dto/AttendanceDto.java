package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDto {
    private Long attendanceId;
    private Long groupId;
    private Long userId;
    private String userName;
    private LocalDate sessionDate;
    private Boolean isPresent;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
}