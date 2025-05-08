// src/main/java/com/mogacko/mogacko/service/GroupNoticeService.java
package com.mogacko.mogacko.service;

import com.mogacko.mogacko.dto.GroupNoticeCreateRequest;
import com.mogacko.mogacko.dto.GroupNoticeDto;
import com.mogacko.mogacko.entity.GroupMember;
import com.mogacko.mogacko.entity.GroupNotice;
import com.mogacko.mogacko.entity.StudyGroup;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserProfile;
import com.mogacko.mogacko.repository.GroupMemberRepository;
import com.mogacko.mogacko.repository.GroupNoticeRepository;
import com.mogacko.mogacko.repository.StudyGroupRepository;
import com.mogacko.mogacko.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupNoticeService {

    private final GroupNoticeRepository noticeRepository;
    private final StudyGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;

    public Page<GroupNoticeDto> getGroupNotices(Long groupId, int page, int size) {
        Optional<StudyGroup> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return null;
        }

        StudyGroup group = groupOpt.get();

        Pageable pageable = PageRequest.of(page, size);
        Page<GroupNotice> noticePage = noticeRepository.findByGroupOrderByCreatedAtDesc(group, pageable);

        return noticePage.map(this::mapToNoticeDto);
    }

    public GroupNoticeDto getNoticeDetails(Long groupId, Long noticeId) {
        Optional<GroupNotice> noticeOpt = noticeRepository.findById(noticeId);

        if (noticeOpt.isEmpty() || !noticeOpt.get().getGroup().getGroupId().equals(groupId)) {
            return null;
        }

        GroupNotice notice = noticeOpt.get();
        return mapToNoticeDto(notice);
    }

    @Transactional
    public GroupNoticeDto createNotice(User user, Long groupId, GroupNoticeCreateRequest request) {
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

        GroupNotice newNotice = GroupNotice.builder()
                .group(group)
                .creator(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        GroupNotice savedNotice = noticeRepository.save(newNotice);

        return mapToNoticeDto(savedNotice);
    }

    @Transactional
    public GroupNoticeDto updateNotice(User user, Long groupId, Long noticeId, GroupNoticeCreateRequest request) {
        Optional<GroupNotice> noticeOpt = noticeRepository.findById(noticeId);

        if (noticeOpt.isEmpty() || !noticeOpt.get().getGroup().getGroupId().equals(groupId)) {
            return null;
        }

        GroupNotice notice = noticeOpt.get();

        // 작성자 또는 그룹 생성자만 수정 가능
        if (!notice.getCreator().getUserId().equals(user.getUserId()) &&
                !notice.getGroup().getCreator().getUserId().equals(user.getUserId())) {
            return null;
        }

        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());

        GroupNotice updatedNotice = noticeRepository.save(notice);

        return mapToNoticeDto(updatedNotice);
    }

    @Transactional
    public boolean deleteNotice(User user, Long groupId, Long noticeId) {
        Optional<GroupNotice> noticeOpt = noticeRepository.findById(noticeId);

        if (noticeOpt.isEmpty() || !noticeOpt.get().getGroup().getGroupId().equals(groupId)) {
            return false;
        }

        GroupNotice notice = noticeOpt.get();

        // 작성자 또는 그룹 생성자만 삭제 가능
        if (!notice.getCreator().getUserId().equals(user.getUserId()) &&
                !notice.getGroup().getCreator().getUserId().equals(user.getUserId())) {
            return false;
        }

        noticeRepository.delete(notice);

        return true;
    }

    private GroupNoticeDto mapToNoticeDto(GroupNotice notice) {
        String creatorName = "";
        Optional<UserProfile> creatorProfile = userProfileRepository.findByUser(notice.getCreator());
        if (creatorProfile.isPresent()) {
            creatorName = creatorProfile.get().getName();
        }

        return GroupNoticeDto.builder()
                .noticeId(notice.getNoticeId())
                .groupId(notice.getGroup().getGroupId())
                .creatorId(notice.getCreator().getUserId())
                .creatorName(creatorName)
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}