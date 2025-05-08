package com.mogacko.mogacko.service;

import com.mogacko.mogacko.dto.GoalDetailDto;
import com.mogacko.mogacko.dto.GroupGoalCreateRequest;
import com.mogacko.mogacko.dto.GroupGoalDto;
import com.mogacko.mogacko.entity.GroupGoal;
import com.mogacko.mogacko.entity.GroupGoalDetail;
import com.mogacko.mogacko.entity.GroupMember;
import com.mogacko.mogacko.entity.StudyGroup;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserProfile;
import com.mogacko.mogacko.repository.GroupGoalDetailRepository;
import com.mogacko.mogacko.repository.GroupGoalRepository;
import com.mogacko.mogacko.repository.GroupMemberRepository;
import com.mogacko.mogacko.repository.StudyGroupRepository;
import com.mogacko.mogacko.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupGoalService {

    private final GroupGoalRepository goalRepository;
    private final GroupGoalDetailRepository detailRepository;
    private final StudyGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;

    public List<GroupGoalDto> getGroupGoals(Long groupId) {
        Optional<StudyGroup> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return new ArrayList<>();
        }

        StudyGroup group = groupOpt.get();
        List<GroupGoal> goals = goalRepository.findByGroupOrderByEndDateDesc(group);

        return goals.stream()
                .map(this::mapToGoalDto)
                .collect(Collectors.toList());
    }

    public GroupGoalDto getGoalDetails(Long groupId, Long goalId) {
        Optional<GroupGoal> goalOpt = goalRepository.findById(goalId);

        if (goalOpt.isEmpty() || !goalOpt.get().getGroup().getGroupId().equals(groupId)) {
            return null;
        }

        GroupGoal goal = goalOpt.get();
        return mapToGoalDto(goal);
    }

    @Transactional
    public GroupGoalDto createGoal(User user, Long groupId, GroupGoalCreateRequest request) {
        Optional<StudyGroup> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return null;
        }

        StudyGroup group = groupOpt.get();

        // 그룹 멤버인지 확인
        Optional<GroupMember> memberOpt = memberRepository.findByGroupAndUser(group, user);

        if (memberOpt.isEmpty() || !"ACTIVE".equals(memberOpt.get().getStatus())) {
            return null;
        }

        GroupGoal newGoal = GroupGoal.builder()
                .group(group)
                .creator(user)
                .title(request.getTitle())
                .pointValue(request.getPointValue())
                .endDate(request.getEndDate())
                .build();

        GroupGoal savedGoal = goalRepository.save(newGoal);
// src/main/java/com/mogacko/mogacko/service/GroupGoalService.java (계속)
        // 세부 목표 추가
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            for (String detailDescription : request.getDetails()) {
                GroupGoalDetail detail = GroupGoalDetail.builder()
                        .goal(savedGoal)
                        .description(detailDescription)
                        .isCompleted(false)
                        .build();

                detailRepository.save(detail);
            }
        }

        return mapToGoalDto(goalRepository.findById(savedGoal.getGoalId()).orElse(savedGoal));
    }

    @Transactional
    public GroupGoalDto updateGoal(User user, Long groupId, Long goalId, GroupGoalCreateRequest request) {
        Optional<GroupGoal> goalOpt = goalRepository.findById(goalId);

        if (goalOpt.isEmpty() || !goalOpt.get().getGroup().getGroupId().equals(groupId)) {
            return null;
        }

        GroupGoal goal = goalOpt.get();

        // 작성자 또는 그룹 생성자만 수정 가능
        if (!goal.getCreator().getUserId().equals(user.getUserId()) &&
                !goal.getGroup().getCreator().getUserId().equals(user.getUserId())) {
            return null;
        }

        goal.setTitle(request.getTitle());
        goal.setPointValue(request.getPointValue());
        goal.setEndDate(request.getEndDate());

        GroupGoal updatedGoal = goalRepository.save(goal);

        // 기존 세부 목표 삭제
        List<GroupGoalDetail> existingDetails = detailRepository.findByGoal(updatedGoal);
        detailRepository.deleteAll(existingDetails);

        // 새 세부 목표 추가
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            for (String detailDescription : request.getDetails()) {
                GroupGoalDetail detail = GroupGoalDetail.builder()
                        .goal(updatedGoal)
                        .description(detailDescription)
                        .isCompleted(false)
                        .build();

                detailRepository.save(detail);
            }
        }

        return mapToGoalDto(goalRepository.findById(updatedGoal.getGoalId()).orElse(updatedGoal));
    }

    @Transactional
    public boolean deleteGoal(User user, Long groupId, Long goalId) {
        Optional<GroupGoal> goalOpt = goalRepository.findById(goalId);

        if (goalOpt.isEmpty() || !goalOpt.get().getGroup().getGroupId().equals(groupId)) {
            return false;
        }

        GroupGoal goal = goalOpt.get();

        // 작성자 또는 그룹 생성자만 삭제 가능
        if (!goal.getCreator().getUserId().equals(user.getUserId()) &&
                !goal.getGroup().getCreator().getUserId().equals(user.getUserId())) {
            return false;
        }

        // 세부 목표 삭제
        List<GroupGoalDetail> details = detailRepository.findByGoal(goal);
        detailRepository.deleteAll(details);

        // 목표 삭제
        goalRepository.delete(goal);

        return true;
    }

    @Transactional
    public boolean toggleGoalDetailCompletion(User user, Long groupId, Long goalId, Long detailId) {
        Optional<GroupGoal> goalOpt = goalRepository.findById(goalId);

        if (goalOpt.isEmpty() || !goalOpt.get().getGroup().getGroupId().equals(groupId)) {
            return false;
        }

        Optional<GroupGoalDetail> detailOpt = detailRepository.findById(detailId);

        if (detailOpt.isEmpty() || !detailOpt.get().getGoal().getGoalId().equals(goalId)) {
            return false;
        }

        GroupGoal goal = goalOpt.get();
        GroupGoalDetail detail = detailOpt.get();

        // 그룹 멤버인지 확인
        Optional<GroupMember> memberOpt = memberRepository.findByGroupAndUser(goal.getGroup(), user);

        if (memberOpt.isEmpty() || !"ACTIVE".equals(memberOpt.get().getStatus())) {
            return false;
        }

        // 상태 토글
        detail.setIsCompleted(!detail.getIsCompleted());
        detailRepository.save(detail);

        return true;
    }

    private GroupGoalDto mapToGoalDto(GroupGoal goal) {
        String creatorName = "";
        Optional<UserProfile> creatorProfile = userProfileRepository.findByUser(goal.getCreator());
        if (creatorProfile.isPresent()) {
            creatorName = creatorProfile.get().getName();
        }

        // 세부 목표 조회
        List<GroupGoalDetail> details = detailRepository.findByGoal(goal);
        List<GoalDetailDto> detailDtos = details.stream()
                .map(detail -> GoalDetailDto.builder()
                        .detailId(detail.getDetailId())
                        .goalId(goal.getGoalId())
                        .description(detail.getDescription())
                        .isCompleted(detail.getIsCompleted())
                        .build())
                .collect(Collectors.toList());

        // 완료된 세부 목표 수 계산
        int completedCount = (int) details.stream()
                .filter(GroupGoalDetail::getIsCompleted)
                .count();

        return GroupGoalDto.builder()
                .goalId(goal.getGoalId())
                .groupId(goal.getGroup().getGroupId())
                .creatorId(goal.getCreator().getUserId())
                .creatorName(creatorName)
                .title(goal.getTitle())
                .pointValue(goal.getPointValue())
                .endDate(goal.getEndDate())
                .details(detailDtos)
                .completedCount(completedCount)
                .totalCount(details.size())
                .build();
    }
}