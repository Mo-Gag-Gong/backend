// src/main/java/com/mogacko/mogacko/dto/AuthResponse.java
package com.mogacko.mogacko.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
}