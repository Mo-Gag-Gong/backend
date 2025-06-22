// src/main/java/com/mogacko/mogacko/service/UserService.java
package com.mogacko.mogacko.service;

import com.mogacko.mogacko.dto.*;
import com.mogacko.mogacko.entity.Interest;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserInterest;
import com.mogacko.mogacko.entity.UserProfile;
import com.mogacko.mogacko.repository.InterestRepository;
import com.mogacko.mogacko.repository.UserInterestRepository;
import com.mogacko.mogacko.repository.UserProfileRepository;
import com.mogacko.mogacko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final InterestRepository interestRepository;
    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final UserStatisticsService statisticsService;

    public UserProfileDto getUserProfile(User user) {
        Optional<UserProfile> profileOpt = userProfileRepository.findByUser(user);

        if (profileOpt.isEmpty()) {
            return null;
        }

        UserProfile profile = profileOpt.get();
        UserProfileDto dto = mapToProfileDto(profile);

        // 관심사 추가
        List<Interest> userInterests = userInterestRepository.findUserInterests(user);
        dto.setInterests(userInterests.stream()
                .map(interest -> new InterestDto(interest.getInterestId(), interest.getInterestName()))
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * 사용자의 프로필과 통계 정보를 함께 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @param isOwnProfile 자신의 프로필인지 여부
     * @return 프로필 및 통계 정보
     */
    public UserProfileWithStatsDto getUserProfileWithStats(Long userId, boolean isOwnProfile) {
        // 사용자 조회
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();

        // 프로필 조회
        Optional<UserProfile> profileOpt = userProfileRepository.findByUser(user);
        if (profileOpt.isEmpty()) {
            return null;
        }

        UserProfile profile = profileOpt.get();

        // 관심사 조회
        List<Interest> userInterests = userInterestRepository.findUserInterests(user);
        List<InterestDto> interestDtos = userInterests.stream()
                .map(interest -> new InterestDto(interest.getInterestId(), interest.getInterestName()))
                .collect(Collectors.toList());

        // 통계 정보 조회
        UserStatisticsDto statistics = statisticsService.getUserStatistics(user);

        // DTO 구성
        UserProfileWithStatsDto.UserProfileWithStatsDtoBuilder builder = UserProfileWithStatsDto.builder()
                .userId(user.getUserId())
                .name(profile.getName())
                .profileImage(user.getProfileImage())
                .interests(interestDtos)
                .isOwnProfile(isOwnProfile);

        // 자신의 프로필인 경우에만 개인정보 포함
        if (isOwnProfile) {
            builder.email(user.getEmail())
                    .gender(profile.getGender())
                    .phoneNumber(profile.getPhoneNumber())
                    .birthDate(profile.getBirthDate());
        }

        // 통계 정보 추가
        if (statistics != null) {
            builder.groupParticipationCount(statistics.getGroupParticipationCount())
                    .attendanceRate(statistics.getAttendanceRate())
                    .totalMeetings(statistics.getTotalMeetings())
                    .statsLastUpdated(statistics.getLastUpdated());
        }

        return builder.build();
    }

    /**
     * 특정 사용자 ID로 프로필 정보를 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 프로필 정보
     */
    public UserProfileDto getUserProfileById(Long userId) {
        // 사용자 조회
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();

        // 프로필 조회
        Optional<UserProfile> profileOpt = userProfileRepository.findByUser(user);
        if (profileOpt.isEmpty()) {
            return null;
        }

        UserProfile profile = profileOpt.get();
        UserProfileDto dto = mapToProfileDto(profile);

        // 관심사 추가
        List<Interest> userInterests = userInterestRepository.findUserInterests(user);
        dto.setInterests(userInterests.stream()
                .map(interest -> new InterestDto(interest.getInterestId(), interest.getInterestName()))
                .collect(Collectors.toList()));

        return dto;
    }

    @Transactional
    public UserProfileDto updateProfile(User user, ProfileUpdateRequest request) {
        Optional<UserProfile> profileOpt = userProfileRepository.findByUser(user);

        if (profileOpt.isEmpty()) {
            return null;
        }

        UserProfile profile = profileOpt.get();
        boolean wasOnboardingIncomplete = !profile.getOnboardingCompleted();

        // 프로필 업데이트
        if (request.getName() != null) profile.setName(request.getName());
        if (request.getGender() != null) profile.setGender(request.getGender());
        if (request.getPhoneNumber() != null) profile.setPhoneNumber(request.getPhoneNumber());
        if (request.getBirthDate() != null) profile.setBirthDate(request.getBirthDate());
        if (request.getLocationName() != null) profile.setLocationName(request.getLocationName());

        // 필수 필드가 모두 입력되었는지 확인 (이름, 성별, 생년월일이 필수라고 가정)
        boolean allRequiredFieldsFilled = profile.getName() != null &&
                profile.getGender() != null &&
                profile.getBirthDate() != null;

        // 필수 필드가 모두 입력되었으면 onboardingCompleted를 true로 설정
        if (allRequiredFieldsFilled) {
            profile.setOnboardingCompleted(true);
        }

        userProfileRepository.save(profile);

        // 추가 정보 입력이 완료되었고, 이전에는 미완료 상태였다면 ROLE 업데이트
        if (profile.getOnboardingCompleted() && wasOnboardingIncomplete) {
            user.setRole("ROLE_USER");
            userRepository.save(user);
        }

        // 관심사 업데이트
        if (request.getInterestIds() != null && !request.getInterestIds().isEmpty()) {
            // 기존 관심사 삭제
            List<UserInterest> existingInterests = userInterestRepository.findByUser(user);
            userInterestRepository.deleteAll(existingInterests);

            // 새 관심사 추가
            for (Long interestId : request.getInterestIds()) {
                interestRepository.findById(interestId).ifPresent(interest -> {
                    UserInterest userInterest = UserInterest.builder()
                            .user(user)
                            .interest(interest)
                            .build();
                    userInterestRepository.save(userInterest);
                });
            }
        }

        UserProfileDto dto = mapToProfileDto(profile);

        // 업데이트된 관심사 추가
        List<Interest> updatedInterests = userInterestRepository.findUserInterests(user);
        dto.setInterests(updatedInterests.stream()
                .map(interest -> new InterestDto(interest.getInterestId(), interest.getInterestName()))
                .collect(Collectors.toList()));

        return dto;
    }

    public List<InterestDto> getAllInterests() {
        List<Interest> interests = interestRepository.findByIsActiveTrue();
        return interests.stream()
                .map(interest -> new InterestDto(interest.getInterestId(), interest.getInterestName()))
                .collect(Collectors.toList());
    }

    public List<InterestDto> getUserInterests(User user) {
        List<Interest> userInterests = userInterestRepository.findUserInterests(user);
        return userInterests.stream()
                .map(interest -> new InterestDto(interest.getInterestId(), interest.getInterestName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean addInterest(User user, Long interestId) {
        Optional<Interest> interestOpt = interestRepository.findById(interestId);
        if (interestOpt.isEmpty()) {
            return false;
        }

        Interest interest = interestOpt.get();

        // 이미 추가된 관심사인지 확인
        Optional<UserInterest> existingOpt = userInterestRepository.findByUserAndInterest(user, interest);
        if (existingOpt.isPresent()) {
            return true; // 이미 존재하면 성공 반환
        }

        // 관심사 추가
        UserInterest userInterest = UserInterest.builder()
                .user(user)
                .interest(interest)
                .build();

        userInterestRepository.save(userInterest);
        return true;
    }

    @Transactional
    public boolean removeInterest(User user, Long interestId) {
        Optional<Interest> interestOpt = interestRepository.findById(interestId);
        if (interestOpt.isEmpty()) {
            return false;
        }

        Interest interest = interestOpt.get();

        // 관심사 제거
        Optional<UserInterest> userInterestOpt = userInterestRepository.findByUserAndInterest(user, interest);
        if (userInterestOpt.isEmpty()) {
            return false;
        }

        userInterestRepository.delete(userInterestOpt.get());
        return true;
    }

    private UserProfileDto mapToProfileDto(UserProfile profile) {
        return UserProfileDto.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUser().getUserId())
                .email(profile.getUser().getEmail())
                .name(profile.getName())
                .gender(profile.getGender())
                .phoneNumber(profile.getPhoneNumber())
                .birthDate(profile.getBirthDate())
                .profileImage(profile.getUser().getProfileImage())
                .build();
    }
}