package com.mogacko.mogacko.service;

import com.mogacko.mogacko.dto.GroupChatCreateRequest;
import com.mogacko.mogacko.dto.GroupChatDto;
import com.mogacko.mogacko.entity.GroupChat;
import com.mogacko.mogacko.entity.GroupMember;
import com.mogacko.mogacko.entity.StudyGroup;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserProfile;
import com.mogacko.mogacko.repository.GroupChatRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChatRepository chatRepository;
    private final StudyGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;

    public Page<GroupChatDto> getGroupChats(Long groupId, int page, int size) {
        Optional<StudyGroup> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return null;
        }

        StudyGroup group = groupOpt.get();

        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<GroupChat> chatPage = chatRepository.findByGroupOrderBySentAtDesc(group, pageable);

        return chatPage.map(this::mapToChatDto);
    }

    @Transactional
    public GroupChatDto sendMessage(User user, Long groupId, GroupChatCreateRequest request) {
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

        GroupChat newChat = GroupChat.builder()
                .group(group)
                .sender(user)
                .message(request.getMessage())
                .sentAt(LocalDateTime.now())
                .build();

        GroupChat savedChat = chatRepository.save(newChat);

        return mapToChatDto(savedChat);
    }

    private GroupChatDto mapToChatDto(GroupChat chat) {
        String userName = "";
        String profileImage = "";

        // 사용자 프로필 정보 조회
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUser(chat.getSender());
        if (userProfileOpt.isPresent()) {
            userName = userProfileOpt.get().getName();
        }

        // 프로필 이미지 조회
        profileImage = chat.getSender().getProfileImage();

        return GroupChatDto.builder()
                .chatId(chat.getChatId())
                .groupId(chat.getGroup().getGroupId())
                .senderId(chat.getSender().getUserId())
                .userName(userName)
                .profileImage(profileImage)
                .message(chat.getMessage())
                .sentAt(chat.getSentAt())
                .build();
    }
}