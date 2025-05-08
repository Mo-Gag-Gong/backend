package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMemberDto {
    private Long membershipId;
    private Long userId;
    private String userName;
    private String profileImage;
    private LocalDate joinDate;
    private String status;
}