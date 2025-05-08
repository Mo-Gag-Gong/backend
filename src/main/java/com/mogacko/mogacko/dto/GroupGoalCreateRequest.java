package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupGoalCreateRequest {
    private String title;
    private Integer pointValue;
    private LocalDate endDate;
    private List<String> details;
}