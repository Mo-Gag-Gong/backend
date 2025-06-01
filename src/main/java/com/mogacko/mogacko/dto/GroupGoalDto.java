package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupGoalDto {
    private Long goalId;
    private Long groupId;
    private Long creatorId;
    private String creatorName;
    private String title;
    private Integer pointValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<GoalDetailDto> details;
    private Integer completedCount;
    private Integer totalCount;
}