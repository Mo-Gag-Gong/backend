// src/main/java/com/mogacko/mogacko/service/MeetingService.java
package com.mogacko.mogacko.service;

import com.mogacko.mogacko.dto.*;
import com.mogacko.mogacko.entity.*;
import com.mogacko.mogacko.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final StudyGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;

    // 모임 목록 조회
    public List<MeetingDto> getMeetings(Long groupId) {
        Optional<StudyGroup> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return List.of();
        }

        StudyGroup group = groupOpt.get();
        List<Meeting> meetings = meetingRepository.findByGroupOrderByMeetingDateDesc(group);

        return meetings.stream()
                .map(this::mapToMeetingDto)
                .collect(Collectors.toList());
    }

    // 모임 상세 조회
    public MeetingDto getMeetingDetails(Long groupId, Long meetingId) {
        Optional<Meeting> meetingOpt = meetingRepository.findById(meetingId);

        if (meetingOpt.isEmpty() || !meetingOpt.get().getGroup().getGroupId().equals(groupId)) {
            return null;
        }

        return mapToMeetingDto(meetingOpt.get());
    }

    // 모임 생성
    @Transactional
    public MeetingDto createMeeting(User user, Long groupId, MeetingCreateRequest request) {
        Optional<StudyGroup> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return null;
        }

        StudyGroup group = groupOpt.get();

        // 멤버 권한 확인
        Optional<GroupMember> memberOpt = memberRepository.findByGroupAndUser(group, user);
        if (memberOpt.isEmpty() || !"ACTIVE".equals(memberOpt.get().getStatus())) {
            return null;
        }

        Meeting meeting = Meeting.builder()
                .group(group)
                .title(request.getTitle())
                .location(request.getLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .meetingDate(request.getMeetingDate())
                .maxParticipants(request.getMaxParticipants())
                .description(request.getDescription())
                .createdBy(user)
                .build();

        Meeting savedMeeting = meetingRepository.save(meeting);

        // 생성자는 자동으로 참가자로 등록
        MeetingParticipant participant = MeetingParticipant.builder()
                .meeting(savedMeeting)
                .user(user)
                .status("REGISTERED")
                .build();

        participantRepository.save(participant);

        return mapToMeetingDto(savedMeeting);
    }

    // 모임 참가
    @Transactional
    public boolean joinMeeting(User user, Long groupId, Long meetingId) {
        Optional<Meeting> meetingOpt = meetingRepository.findById(meetingId);

        if (meetingOpt.isEmpty() || !meetingOpt.get().getGroup().getGroupId().equals(groupId)) {
            return false;
        }

        Meeting meeting = meetingOpt.get();

        // 그룹 멤버인지 확인
        Optional<GroupMember> memberOpt = memberRepository.findByGroupAndUser(meeting.getGroup(), user);
        if (memberOpt.isEmpty() || !"ACTIVE".equals(memberOpt.get().getStatus())) {
            return false;
        }

        // 이미 참가했는지 확인
        Optional<MeetingParticipant> existingOpt = participantRepository.findByMeetingAndUser(meeting, user);
        if (existingOpt.isPresent()) {
            return true;
        }

        // 최대 인원 확인
        int currentParticipants = participantRepository.countParticipants(meeting);
        if (meeting.getMaxParticipants() != null && currentParticipants >= meeting.getMaxParticipants()) {
            return false;
        }

        MeetingParticipant participant = MeetingParticipant.builder()
                .meeting(meeting)
                .user(user)
                .status("REGISTERED")
                .build();

        participantRepository.save(participant);
        return true;
    }

    // 출석 체크
    @Transactional
    public CheckInResponse checkIn(User user, Long groupId, Long meetingId, CheckInRequest request) {
        Optional<Meeting> meetingOpt = meetingRepository.findById(meetingId);

        if (meetingOpt.isEmpty() || !meetingOpt.get().getGroup().getGroupId().equals(groupId)) {
            return CheckInResponse.builder()
                    .success(false)
                    .message("모임을 찾을 수 없습니다.")
                    .build();
        }

        Meeting meeting = meetingOpt.get();

        // 참가자인지 확인
        Optional<MeetingParticipant> participantOpt = participantRepository.findByMeetingAndUser(meeting, user);
        if (participantOpt.isEmpty()) {
            return CheckInResponse.builder()
                    .success(false)
                    .message("모임에 참가하지 않았습니다.")
                    .build();
        }

        // 현재 시간이 모임 시간대인지 확인 (±2시간)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime meetingStart = meeting.getMeetingDate().minusHours(2);
        LocalDateTime meetingEnd = meeting.getMeetingDate().plusHours(2);

        if (now.isBefore(meetingStart) || now.isAfter(meetingEnd)) {
            return CheckInResponse.builder()
                    .success(false)
                    .message("출석체크 가능 시간이 아닙니다.")
                    .build();
        }

        // 거리 계산 (100m 이내인지 확인)
        double distance = calculateDistance(
                meeting.getLatitude().doubleValue(),
                meeting.getLongitude().doubleValue(),
                request.getLatitude().doubleValue(),
                request.getLongitude().doubleValue()
        );

        if (distance > 100) {
            return CheckInResponse.builder()
                    .success(false)
                    .message("모임 장소에서 너무 멀리 떨어져 있습니다.")
                    .build();
        }

        // 출석 체크
        MeetingParticipant participant = participantOpt.get();
        participant.setStatus("ATTENDED");
        participant.setCheckInTime(now);
        participantRepository.save(participant);

        return CheckInResponse.builder()
                .success(true)
                .message("출석체크가 완료되었습니다.")
                .build();
    }

    // 거리 계산 (Haversine formula)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반경 (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // 미터로 변환
        return distance;
    }

    private MeetingDto mapToMeetingDto(Meeting meeting) {
        String creatorName = "";
        Optional<UserProfile> creatorProfile = userProfileRepository.findByUser(meeting.getCreatedBy());
        if (creatorProfile.isPresent()) {
            creatorName = creatorProfile.get().getName();
        }

        int participantCount = participantRepository.countParticipants(meeting);

        return MeetingDto.builder()
                .meetingId(meeting.getMeetingId())
                .groupId(meeting.getGroup().getGroupId())
                .title(meeting.getTitle())
                .location(meeting.getLocation())
                .latitude(meeting.getLatitude())
                .longitude(meeting.getLongitude())
                .meetingDate(meeting.getMeetingDate())
                .maxParticipants(meeting.getMaxParticipants())
                .description(meeting.getDescription())
                .createdBy(meeting.getCreatedBy().getUserId())
                .creatorName(creatorName)
                .participantCount(participantCount)
                .createdAt(meeting.getCreatedAt())
                .build();
    }
}