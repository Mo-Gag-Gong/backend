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
public class StudyGroupDto {
    private Long groupId;
    private Long creatorId;
    private String creatorName;
    private String title;
    private String interestName;
    private String description;
    private String locationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxMembers;
    private Integer currentMembers;
    private String requirements;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}