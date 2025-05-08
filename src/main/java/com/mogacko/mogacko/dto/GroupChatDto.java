// src/main/java/com/mogacko/mogacko/dto/GroupChatDto.java
package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupChatDto {
    private Long chatId;
    private Long groupId;
    private Long userId;
    private String userName;
    private String profileImage;
    private String message;
    private LocalDateTime sentAt;
}