package com.mogacko.mogacko.service;

import com.mogacko.mogacko.dto.GroupCreateRequest;
import com.mogacko.mogacko.dto.GroupMemberDto;
import com.mogacko.mogacko.dto.StudyGroupDto;
import com.mogacko.mogacko.entity.GroupMember;
import com.mogacko.mogacko.entity.StudyGroup;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserProfile;
import com.mogacko.mogacko.repository.GroupMemberRepository;
import com.mogacko.mogacko.repository.StudyGroupRepository;
import com.mogacko.mogacko.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserProfileRepository userProfileRepository;

    public Page<StudyGroupDto> getAllGroups(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StudyGroup> groupPage = studyGroupRepository.findActiveGroups(pageable);

        return groupPage.map(group -> {
            int currentMembers = groupMemberRepository.countActiveMembers(group);
            return mapToGroupDto(group, currentMembers);
        });
    }

    public Page<StudyGroupDto> getGroupsByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StudyGroup> groupPage = studyGroupRepository.findActiveGroupsByCategory(category, pageable);

        return groupPage.map(group -> {
            int currentMembers = groupMemberRepository.countActiveMembers(group);
            return mapToGroupDto(group, currentMembers);
        });
    }

    public Page<StudyGroupDto> searchGroups(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StudyGroup> groupPage = studyGroupRepository.searchByKeyword(keyword, pageable);

        return groupPage.map(group -> {
            int currentMembers = groupMemberRepository.countActiveMembers(group);
            return mapToGroupDto(group, currentMembers);
        });
    }

    public List<StudyGroupDto> getMyGroups(User user) {
        List<StudyGroup> userGroups = groupMemberRepository.findUserGroups(user);

        return userGroups.stream()
                .map(group -> {
                    int currentMembers = groupMemberRepository.countActiveMembers(group);
                    return mapToGroupDto(group, currentMembers);
                })
                .collect(Collectors.toList());
    }

    public StudyGroupDto getGroupDetails(Long groupId) {
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return null;
        }

        StudyGroup group = groupOpt.get();
        int currentMembers = groupMemberRepository.countActiveMembers(group);
        return mapToGroupDto(group, currentMembers);
    }

    @Transactional
    public StudyGroupDto createGroup(User user, GroupCreateRequest request) {
        // 스터디 그룹 생성
        StudyGroup newGroup = StudyGroup.builder()
                .creator(user)
                .title(request.getTitle())
                .category(request.getCategory())
                .description(request.getDescription())
                .locationName(request.getLocationName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .maxMembers(request.getMaxMembers())
                .requirements(request.getRequirements())
                .isActive(true)
                .build();

        StudyGroup savedGroup = studyGroupRepository.save(newGroup);

        // 생성자를 그룹 멤버로 추가
        GroupMember creatorMember = GroupMember.builder()
                .group(savedGroup)
                .user(user)
                .joinDate(LocalDate.now())
                .status("ACTIVE")
                .build();

        groupMemberRepository.save(creatorMember);

        return mapToGroupDto(savedGroup, 1); // 생성자 1명으로 시작
    }

    @Transactional
    public StudyGroupDto updateGroup(User user, Long groupId, GroupCreateRequest request) {
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return null;
        }

        StudyGroup group = groupOpt.get();

        // 생성자만 수정 가능
        if (!group.getCreator().getUserId().equals(user.getUserId())) {
            return null;
        }

        // 그룹 정보 업데이트
        group.setTitle(request.getTitle());
        group.setCategory(request.getCategory());
        group.setDescription(request.getDescription());
        group.setLocationName(request.getLocationName());
        group.setLatitude(request.getLatitude());
        group.setLongitude(request.getLongitude());
        group.setStartDate(request.getStartDate());
        group.setEndDate(request.getEndDate());
        group.setMaxMembers(request.getMaxMembers());
        group.setRequirements(request.getRequirements());

        StudyGroup updatedGroup = studyGroupRepository.save(group);
        int currentMembers = groupMemberRepository.countActiveMembers(updatedGroup);

        return mapToGroupDto(updatedGroup, currentMembers);
    }

    @Transactional
    public boolean deactivateGroup(User user, Long groupId) {
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return false;
        }

        StudyGroup group = groupOpt.get();

        // 생성자만 비활성화 가능
        if (!group.getCreator().getUserId().equals(user.getUserId())) {
            return false;
        }

        // 그룹 비활성화
        group.setIsActive(false);
        studyGroupRepository.save(group);

        return true;
    }

    public List<GroupMemberDto> getGroupMembers(Long groupId) {
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return null;
        }

        StudyGroup group = groupOpt.get();
        List<GroupMember> members = groupMemberRepository.findByGroup(group);

        return members.stream()
                .map(this::mapToMemberDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean joinGroup(User user, Long groupId) {
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return false;
        }

        StudyGroup group = groupOpt.get();

        // 이미 가입한 멤버인지 확인
        Optional<GroupMember> existingMemberOpt = groupMemberRepository.findByGroupAndUser(group, user);

        if (existingMemberOpt.isPresent()) {
            GroupMember existingMember = existingMemberOpt.get();

            // 이미 활성 멤버면 무시
            if ("ACTIVE".equals(existingMember.getStatus())) {
                return true;
            }

            // 탈퇴했던 멤버면 상태 업데이트
            existingMember.setStatus("ACTIVE");
            existingMember.setJoinDate(LocalDate.now());
            groupMemberRepository.save(existingMember);
        } else {
            // 현재 인원 확인
            int currentMembers = groupMemberRepository.countActiveMembers(group);

            // 최대 인원 초과 체크
            if (group.getMaxMembers() != null && currentMembers >= group.getMaxMembers()) {
                return false;
            }

            // 새 멤버 추가
            GroupMember newMember = GroupMember.builder()
                    .group(group)
                    .user(user)
                    .joinDate(LocalDate.now())
                    .status("ACTIVE")
                    .build();

            groupMemberRepository.save(newMember);
        }

        return true;
    }

    @Transactional
    public boolean leaveGroup(User user, Long groupId) {
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return false;
        }

        StudyGroup group = groupOpt.get();

        // 생성자는 떠날 수 없음
        if (group.getCreator().getUserId().equals(user.getUserId())) {
            return false;
        }

        // 멤버 조회
        Optional<GroupMember> memberOpt = groupMemberRepository.findByGroupAndUser(group, user);

        if (memberOpt.isEmpty()) {
            return false;
        }

        GroupMember member = memberOpt.get();
        member.setStatus("INACTIVE");
        groupMemberRepository.save(member);

        return true;
    }

    private StudyGroupDto mapToGroupDto(StudyGroup group, int currentMembers) {
        // 생성자 이름 조회
        String creatorName = "";
        Optional<UserProfile> creatorProfile = userProfileRepository.findByUser(group.getCreator());
        if (creatorProfile.isPresent()) {
            creatorName = creatorProfile.get().getName();
        }

        return StudyGroupDto.builder()
                .groupId(group.getGroupId())
                .creatorId(group.getCreator().getUserId())
                .creatorName(creatorName)
                .title(group.getTitle())
                .category(group.getCategory())
                .description(group.getDescription())
                .locationName(group.getLocationName())
                .latitude(group.getLatitude())
                .longitude(group.getLongitude())
                .startDate(group.getStartDate())
                .endDate(group.getEndDate())
                .maxMembers(group.getMaxMembers())
                .currentMembers(currentMembers)
                .requirements(group.getRequirements())
                .isActive(group.getIsActive())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }

    private GroupMemberDto mapToMemberDto(GroupMember member) {
        String userName = "";
        String profileImage = "";

        // 사용자 프로필 정보 조회
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUser(member.getUser());
        if (userProfileOpt.isPresent()) {
            userName = userProfileOpt.get().getName();
        }

        // 프로필 이미지 조회
        profileImage = member.getUser().getProfileImage();

        return GroupMemberDto.builder()
                .membershipId(member.getMembershipId())
                .userId(member.getUser().getUserId())
                .userName(userName)
                .profileImage(profileImage)
                .joinDate(member.getJoinDate())
                .status(member.getStatus())
                .build();
    }
}