// src/main/java/com/mogacko/mogacko/service/UserService.java
package com.mogacko.mogacko.service;

import com.mogacko.mogacko.dto.InterestDto;
import com.mogacko.mogacko.dto.ProfileUpdateRequest;
import com.mogacko.mogacko.dto.UserProfileDto;
import com.mogacko.mogacko.entity.Interest;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserInterest;
import com.mogacko.mogacko.entity.UserProfile;
import com.mogacko.mogacko.repository.InterestRepository;
import com.mogacko.mogacko.repository.UserInterestRepository;
import com.mogacko.mogacko.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;

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

    @Transactional
    public UserProfileDto updateProfile(User user, ProfileUpdateRequest request) {
        Optional<UserProfile> profileOpt = userProfileRepository.findByUser(user);

        if (profileOpt.isEmpty()) {
            return null;
        }

        UserProfile profile = profileOpt.get();

        // 프로필 업데이트
        if (request.getName() != null) profile.setName(request.getName());
        if (request.getGender() != null) profile.setGender(request.getGender());
        if (request.getPhoneNumber() != null) profile.setPhoneNumber(request.getPhoneNumber());
        if (request.getBirthYear() != null) profile.setBirthYear(request.getBirthYear());

        userProfileRepository.save(profile);

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
                .birthYear(profile.getBirthYear())
                .mentorScore(profile.getMentorScore())
                .profileImage(profile.getUser().getProfileImage())
                .build();
    }
}