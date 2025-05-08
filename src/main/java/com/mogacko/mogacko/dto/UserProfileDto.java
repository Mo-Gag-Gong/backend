// src/main/java/com/mogacko/mogacko/dto/UserProfileDto.java
package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    private Long profileId;
    private Long userId;
    private String email;
    private String name;
    private String gender;
    private String phoneNumber;
    private Integer birthYear;
    private Integer mentorScore;
    private String profileImage;
    private List<InterestDto> interests;
}