package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingCreateRequest {
    private String title;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime meetingDate;
    private Integer maxParticipants;
    private String description;
}
