package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    private String name;
    private String gender;
    private String phoneNumber;
    private Integer birthYear;
    private List<Long> interestIds;
}