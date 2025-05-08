package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatisticsDto {
    private Long statId;
    private Long userId;
    private Integer groupParticipationCount;
    private Double attendanceRate;
    private Integer totalStudySessions;
    private LocalDateTime lastUpdated;
}