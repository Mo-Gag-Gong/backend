package com.mogacko.mogacko.service;

import com.mogacko.mogacko.dto.*;
import com.mogacko.mogacko.entity.*;
import com.mogacko.mogacko.exception.ResourceNotFoundException;
import com.mogacko.mogacko.repository.*;
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
    private final InterestRepository interestRepository;
    private final UserRepository userRepository;

    public Page<StudyGroupDto> getAllGroups(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StudyGroup> groupPage = studyGroupRepository.findActiveGroups(pageable);

        return groupPage.map(group -> {
            int currentMembers = groupMemberRepository.countActiveMembers(group);
            return mapToGroupDto(group, currentMembers);
        });
    }

    public Page<StudyGroupDto> getGroupsByInterest(String interestName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StudyGroup> groupPage = studyGroupRepository.findActiveGroupsByInterestName(interestName, pageable);

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

    /**
     * 스터디 그룹에서 특정 멤버를 추방합니다.
     *
     * @param currentUser 현재 사용자 (그룹 생성자여야 함)
     * @param groupId 스터디 그룹 ID
     * @param userId 추방할 사용자 ID
     * @return 추방 결과
     */
    @Transactional
    public KickMemberResult kickMember(User currentUser, Long groupId, Long userId) {
        // 1. 그룹 존재 여부 확인
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return KickMemberResult.GROUP_NOT_FOUND;
        }

        StudyGroup group = groupOpt.get();

        // 2. 현재 사용자가 그룹 생성자인지 확인
        if (!group.getCreator().getUserId().equals(currentUser.getUserId())) {
            return KickMemberResult.NOT_GROUP_OWNER;
        }

        // 3. 추방할 사용자 존재 여부 확인
        Optional<User> targetUserOpt = userRepository.findById(userId);
        if (targetUserOpt.isEmpty()) {
            return KickMemberResult.USER_NOT_FOUND;
        }

        User targetUser = targetUserOpt.get();

        // 4. 그룹 생성자는 추방할 수 없음
        if (group.getCreator().getUserId().equals(targetUser.getUserId())) {
            return KickMemberResult.CANNOT_KICK_OWNER;
        }

        // 5. 대상 사용자가 그룹 멤버인지 확인
        Optional<GroupMember> memberOpt = groupMemberRepository.findByGroupAndUser(group, targetUser);
        if (memberOpt.isEmpty()) {
            return KickMemberResult.MEMBER_NOT_FOUND;
        }

        GroupMember member = memberOpt.get();

        // 6. 이미 탈퇴한 멤버인지 확인
        if (!"ACTIVE".equals(member.getStatus())) {
            return KickMemberResult.MEMBER_NOT_FOUND;
        }

        // 7. 멤버 상태를 KICKED로 변경
        member.setStatus("KICKED");
        groupMemberRepository.save(member);

        return KickMemberResult.SUCCESS;
    }

    /**
     * 사용자가 그룹장(생성자)인 스터디 그룹 목록을 조회합니다.
     *
     * @param user 현재 사용자
     * @return 사용자가 그룹장인 스터디 그룹 목록
     */
    public List<StudyGroupDto> getMyOwnedGroups(User user) {
        List<StudyGroup> ownedGroups = studyGroupRepository.findByCreator(user);

        return ownedGroups.stream()
                .filter(StudyGroup::getIsActive) // 활성 그룹만 조회
                .map(group -> {
                    int currentMembers = groupMemberRepository.countActiveMembers(group);
                    return mapToGroupDto(group, currentMembers);
                })
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 참여자로 있는 스터디 그룹 목록을 조회합니다.
     *
     * @param user 현재 사용자
     * @return 사용자가 참여한 스터디 그룹 목록
     */
    public List<StudyGroupDto> getMyJoinedGroups(User user) {
        List<StudyGroup> joinedGroups = groupMemberRepository.findUserGroups(user);

        return joinedGroups.stream()
                .filter(group -> !group.getCreator().getUserId().equals(user.getUserId())) // 그룹장인 경우 제외
                .filter(StudyGroup::getIsActive) // 활성 그룹만 조회
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
        Interest requestInterest = interestRepository.findByInterestName(request.getInterest()).orElseThrow(()->new ResourceNotFoundException("찾을 수 없는 관심사 입니다."));

        // 스터디 그룹 생성
        StudyGroup newGroup = StudyGroup.builder()
                .creator(user)
                .title(request.getTitle())
                .interest(requestInterest)
                .description(request.getDescription())
                .locationName(request.getLocationName())
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

        Interest requestInterest = interestRepository.findByInterestName(request.getInterest()).orElseThrow(()->new ResourceNotFoundException("찾을 수 없는 관심사 입니다."));


        // 그룹 정보 업데이트
        group.setTitle(request.getTitle());
        group.setInterest(requestInterest);
        group.setDescription(request.getDescription());
        group.setLocationName(request.getLocationName());
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

    /**
     * 스터디 그룹의 가입 대기 멤버 목록을 조회합니다.
     *
     * @param currentUser 현재 사용자 (그룹 생성자여야 함)
     * @param groupId 스터디 그룹 ID
     * @return 가입 대기 멤버 목록 결과
     */
    public PendingMembersResult getPendingMembers(User currentUser, Long groupId) {
        // 1. 그룹 존재 여부 확인
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return PendingMembersResult.error(PendingMembersError.GROUP_NOT_FOUND);
        }

        StudyGroup group = groupOpt.get();

        // 2. 현재 사용자가 그룹 생성자인지 확인
        if (!group.getCreator().getUserId().equals(currentUser.getUserId())) {
            return PendingMembersResult.error(PendingMembersError.NOT_GROUP_OWNER);
        }

        // 3. 가입 대기 상태의 멤버 조회
        List<GroupMember> pendingMembers = groupMemberRepository.findByGroupAndStatus(group, "PENDING");

        // 4. DTO로 변환
        List<GroupMemberDto> pendingMemberDtos = pendingMembers.stream()
                .map(this::mapToMemberDto)
                .collect(Collectors.toList());

        return PendingMembersResult.success(pendingMemberDtos);
    }

    @Transactional
    public boolean applyToGroup(User user, Long groupId) {
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
                return false;
            }

            // 이미 활성 멤버면 무시
            if ("KICKED".equals(existingMember.getStatus())) {
                return false;
            }

            // 이미 대기 중이면 무시
            if ("PENDING".equals(existingMember.getStatus())) {
                return false;
            }

            // 탈퇴했거나 추방당했던 멤버면 대기 상태로 변경
            existingMember.setStatus("PENDING");
            existingMember.setJoinDate(LocalDate.now());
            groupMemberRepository.save(existingMember);
        } else {
            // 현재 인원 확인 (가입 대기 멤버는 인원 수에 포함하지 않음)
            int currentMembers = groupMemberRepository.countActiveMembers(group);

            // 최대 인원 초과 체크
            if (group.getMaxMembers() != null && currentMembers >= group.getMaxMembers()) {
                return false;
            }

            // 새 멤버 추가 (가입 대기 상태로)
            GroupMember newMember = GroupMember.builder()
                    .group(group)
                    .user(user)
                    .joinDate(LocalDate.now())
                    .status("PENDING")
                    .build();

            groupMemberRepository.save(newMember);
        }

        return true;
    }

    /**
     * 가입 신청을 승인합니다.
     *
     * @param currentUser 현재 사용자 (그룹 생성자여야 함)
     * @param groupId 스터디 그룹 ID
     * @param userId 승인할 사용자 ID
     * @return 처리 결과
     */
    @Transactional
    public MembershipActionResult approveMember(User currentUser, Long groupId, Long userId) {
        // 1. 그룹 존재 여부 확인
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return MembershipActionResult.GROUP_NOT_FOUND;
        }

        StudyGroup group = groupOpt.get();

        // 2. 현재 사용자가 그룹 생성자인지 확인
        if (!group.getCreator().getUserId().equals(currentUser.getUserId())) {
            return MembershipActionResult.NOT_GROUP_OWNER;
        }

        // 3. 승인할 사용자 존재 여부 확인
        Optional<User> targetUserOpt = userRepository.findById(userId);
        if (targetUserOpt.isEmpty()) {
            return MembershipActionResult.USER_NOT_FOUND;
        }

        User targetUser = targetUserOpt.get();

        // 4. 가입 신청 상태인지 확인
        Optional<GroupMember> memberOpt = groupMemberRepository.findByGroupAndUser(group, targetUser);
        if (memberOpt.isEmpty() || !"PENDING".equals(memberOpt.get().getStatus())) {
            return MembershipActionResult.MEMBER_NOT_PENDING;
        }

        GroupMember member = memberOpt.get();

        // 5. 현재 활성 멤버 수 확인
        int currentMembers = groupMemberRepository.countActiveMembers(group);

        // 6. 최대 인원 초과 체크
        if (group.getMaxMembers() != null && currentMembers >= group.getMaxMembers()) {
            return MembershipActionResult.MAX_MEMBERS_EXCEEDED;
        }

        // 7. 멤버 상태를 ACTIVE로 변경
        member.setStatus("ACTIVE");
        member.setJoinDate(LocalDate.now()); // 승인 날짜로 갱신
        groupMemberRepository.save(member);

        return MembershipActionResult.SUCCESS;
    }

    /**
     * 가입 신청을 거절합니다.
     *
     * @param currentUser 현재 사용자 (그룹 생성자여야 함)
     * @param groupId 스터디 그룹 ID
     * @param userId 거절할 사용자 ID
     * @return 처리 결과
     */
    @Transactional
    public MembershipActionResult rejectMember(User currentUser, Long groupId, Long userId) {
        // 1. 그룹 존재 여부 확인
        Optional<StudyGroup> groupOpt = studyGroupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return MembershipActionResult.GROUP_NOT_FOUND;
        }

        StudyGroup group = groupOpt.get();

        // 2. 현재 사용자가 그룹 생성자인지 확인
        if (!group.getCreator().getUserId().equals(currentUser.getUserId())) {
            return MembershipActionResult.NOT_GROUP_OWNER;
        }

        // 3. 거절할 사용자 존재 여부 확인
        Optional<User> targetUserOpt = userRepository.findById(userId);
        if (targetUserOpt.isEmpty()) {
            return MembershipActionResult.USER_NOT_FOUND;
        }

        User targetUser = targetUserOpt.get();

        // 4. 가입 신청 상태인지 확인
        Optional<GroupMember> memberOpt = groupMemberRepository.findByGroupAndUser(group, targetUser);
        if (memberOpt.isEmpty() || !"PENDING".equals(memberOpt.get().getStatus())) {
            return MembershipActionResult.MEMBER_NOT_PENDING;
        }

        GroupMember member = memberOpt.get();

        // 5. 가입 신청 삭제 (또는 상태를 REJECTED로 변경)
        groupMemberRepository.delete(member);
        // 또는 거절 이력을 남기고 싶다면:
        // member.setStatus("REJECTED");
        // groupMemberRepository.save(member);

        return MembershipActionResult.SUCCESS;
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
                .interestName(group.getInterest().getInterestName())
                .description(group.getDescription())
                .locationName(group.getLocationName())
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