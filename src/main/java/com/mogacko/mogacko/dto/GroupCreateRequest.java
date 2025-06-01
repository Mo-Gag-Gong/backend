package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupCreateRequest {
    private String title;
    private String interest;
    private String description;
    private String locationName;
    private Integer maxMembers;
    private String requirements;
}