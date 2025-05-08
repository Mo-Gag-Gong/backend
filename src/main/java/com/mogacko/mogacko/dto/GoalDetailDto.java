package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalDetailDto {
    private Long detailId;
    private Long goalId;
    private String description;
    private Boolean isCompleted;
}