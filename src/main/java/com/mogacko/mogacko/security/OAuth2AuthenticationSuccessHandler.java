package com.mogacko.mogacko.security;

import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserProfile;
import com.mogacko.mogacko.repository.UserProfileRepository;
import com.mogacko.mogacko.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    private final String MOBILE_REDIRECT_URI = "com.mogacko://oauth2callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String token = tokenProvider.generateToken(user);

            // 온보딩 상태 확인
            Optional<UserProfile> profileOpt = userProfileRepository.findByUser(user);
            boolean onboardingCompleted = profileOpt.isPresent() && profileOpt.get().getOnboardingCompleted();

            String targetUrl = UriComponentsBuilder.fromUriString(MOBILE_REDIRECT_URI)
                    .queryParam("token", token)
                    .queryParam("userId", user.getUserId())
                    .queryParam("onboardingCompleted", onboardingCompleted)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            // 사용자를 찾을 수 없는 경우 처리
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
        }
    }
}