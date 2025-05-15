package com.mogacko.mogacko.security;

import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserProfile;
import com.mogacko.mogacko.entity.UserStatistics;
import com.mogacko.mogacko.repository.UserProfileRepository;
import com.mogacko.mogacko.repository.UserRepository;
import com.mogacko.mogacko.repository.UserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserStatisticsRepository userStatisticsRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Google 공급자에 맞게 데이터 추출
        OAuthAttributes extractedAttributes = extractAttributes(registrationId, userNameAttributeName, attributes);

        User user = saveOrUpdateUser(extractedAttributes, registrationId);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole())),
                extractedAttributes.getAttributes(),
                extractedAttributes.getNameAttributeKey()
        );
    }

    private OAuthAttributes extractAttributes(String registrationId, String userNameAttributeName,
                                              Map<String, Object> attributes) {
        if("google".equals(registrationId)) {
            return OAuthAttributes.builder()
                    .nameAttributeKey(userNameAttributeName)
                    .attributes(attributes)
                    .name((String) attributes.get("name"))
                    .email((String) attributes.get("email"))
                    .picture((String) attributes.get("picture"))
                    .build();
        }

        // 다른 공급자 추가 시 여기에 구현

        throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
    }


    @Transactional
    public User saveOrUpdateUser(OAuthAttributes attributes, String provider) {
        Optional<User> existingUser = userRepository.findByEmail(attributes.getEmail());

        if (existingUser.isPresent()) {
            // 기존 사용자는 업데이트만 수행
            User user = existingUser.get();
            if (user.getProfileImage() == null || !user.getProfileImage().equals(attributes.getPicture())) {
                user.setProfileImage(attributes.getPicture());
                userRepository.save(user);
            }
            return user;
        } else {
            // 새 사용자 생성 - ROLE_GUEST로 설정
            User newUser = User.builder()
                    .email(attributes.getEmail())
                    .profileImage(attributes.getPicture())
                    .oauthId(attributes.getNameAttributeKey())
                    .provider(provider)
                    .role("ROLE_GUEST")
                    .build();

            User savedUser = userRepository.save(newUser);

            // 1. 사용자 프로필 생성
            Optional<UserProfile> existingProfile = userProfileRepository.findByUser(savedUser);

            if (existingProfile.isEmpty()) {
                UserProfile profile = UserProfile.builder()
                        .user(savedUser)
                        .name(attributes.getName())
                        .onboardingCompleted(false)
                        .build();

                userProfileRepository.save(profile);
            }

            // 2. 사용자 통계 레코드 생성
            Optional<UserStatistics> existingStats = userStatisticsRepository.findByUser(savedUser);

            if (existingStats.isEmpty()) {
                UserStatistics statistics = UserStatistics.builder()
                        .user(savedUser)
                        .groupParticipationCount(0)
                        .attendanceRate(0.0)
                        .totalMeetings(0)
                        .lastUpdated(LocalDateTime.now())
                        .build();

                userStatisticsRepository.save(statistics);
            }

            return savedUser;
        }
    }
}