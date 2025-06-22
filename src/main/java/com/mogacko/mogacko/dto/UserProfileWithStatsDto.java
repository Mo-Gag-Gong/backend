package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileWithStatsDto {
    // 프로필 정보
    private Long userId;
    private String name;
    private String email;  // 자신의 프로필인 경우에만 포함
    private String gender;  // 자신의 프로필인 경우에만 포함
    private String phoneNumber;  // 자신의 프로필인 경우에만 포함
    private LocalDate birthDate;  // 자신의 프로필인 경우에만 포함
    private String profileImage;
    private List<InterestDto> interests;

    // 통계 정보
    private Integer groupParticipationCount;
    private Double attendanceRate;
    private Integer totalMeetings;
    private LocalDateTime statsLastUpdated;

    // 추가 정보
    private Boolean isOwnProfile;  // 자신의 프로필인지 여부
}