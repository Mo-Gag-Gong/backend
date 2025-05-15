package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingDto {
    private Long meetingId;
    private Long groupId;
    private String title;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime meetingDate;
    private Integer maxParticipants;
    private String description;
    private Long createdBy;
    private String creatorName;
    private Integer participantCount;
    private LocalDateTime createdAt;
}