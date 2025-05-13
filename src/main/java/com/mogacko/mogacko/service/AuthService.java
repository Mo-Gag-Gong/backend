package com.mogacko.mogacko.service;

import com.mogacko.mogacko.dto.AuthResponse;
import com.mogacko.mogacko.dto.TokenRequest;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.repository.UserRepository;
import com.mogacko.mogacko.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public AuthResponse refreshToken(TokenRequest request) {
        // Refresh Token 검증
        if (!tokenProvider.validateRefreshToken(request.getRefreshToken())) {
            return null;
        }

        Long userId = Long.parseLong(tokenProvider.getClaims(request.getRefreshToken()).getSubject());
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        // 새로운 Access Token과 Refresh Token 발급
        String newAccessToken = tokenProvider.generateAccessToken(user);
        String newRefreshToken = tokenProvider.generateRefreshToken(user);

        return new AuthResponse(newAccessToken, newRefreshToken, user.getUserId());
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}