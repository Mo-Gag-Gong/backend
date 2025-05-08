package com.mogacko.mogacko.service;

import com.mogacko.mogacko.dto.AttendanceDto;
import com.mogacko.mogacko.dto.CheckInRequest;
import com.mogacko.mogacko.entity.Attendance;
import com.mogacko.mogacko.entity.GroupMember;
import com.mogacko.mogacko.entity.StudyGroup;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserProfile;
import com.mogacko.mogacko.repository.AttendanceRepository;
import com.mogacko.mogacko.repository.GroupMemberRepository;
import com.mogacko.mogacko.repository.StudyGroupRepository;
import com.mogacko.mogacko.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudyGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final UserProfileRepository userProfileRepository;

    public List<AttendanceDto> getGroupAttendance(Long groupId, LocalDate date) {
        Optional<StudyGroup> groupOpt = groupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return null;
        }

        StudyGroup group = groupOpt.get();
        List<Attendance> attendances = attendanceRepository.findByGroupAndSessionDate(group, date);

        return attendances.stream()
                .map(this::mapToAttendanceDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AttendanceDto checkIn(User user, Long groupId, CheckInRequest request) {
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

        // 출석 기록 조회 또는 생성
        Optional<Attendance> attendanceOpt = attendanceRepository.findByGroupAndUserAndSessionDate(
                group, user, request.getSessionDate());

        Attendance attendance;

        if (attendanceOpt.isPresent()) {
            attendance = attendanceOpt.get();

            // 이미 체크인한 경우
            if (attendance.getCheckInTime() != null) {
                return mapToAttendanceDto(attendance);
            }

            // 체크인 시간 업데이트
            attendance.setCheckInTime(LocalDateTime.now());
            attendance.setIsPresent(true);
        } else {
            // 새 출석 기록 생성
            attendance = Attendance.builder()
                    .group(group)
                    .user(user)
                    .sessionDate(request.getSessionDate())
                    .isPresent(true)
                    .checkInTime(LocalDateTime.now())
                    .build();
        }

        Attendance savedAttendance = attendanceRepository.save(attendance);

        return mapToAttendanceDto(savedAttendance);
    }

    @Transactional
    public AttendanceDto checkOut(User user, Long groupId, CheckInRequest request) {
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

        // 출석 기록 조회
        Optional<Attendance> attendanceOpt = attendanceRepository.findByGroupAndUserAndSessionDate(
                group, user, request.getSessionDate());

        if (attendanceOpt.isEmpty() || attendanceOpt.get().getCheckInTime() == null) {
            return null; // 체크인 기록이 없으면 체크아웃 불가
        }

        Attendance attendance = attendanceOpt.get();
        attendance.setCheckOutTime(LocalDateTime.now());

        Attendance savedAttendance = attendanceRepository.save(attendance);

        return mapToAttendanceDto(savedAttendance);
    }

    private AttendanceDto mapToAttendanceDto(Attendance attendance) {
        String userName = "";

        // 사용자 프로필 정보 조회
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUser(attendance.getUser());
        if (userProfileOpt.isPresent()) {
            userName = userProfileOpt.get().getName();
        }

        return AttendanceDto.builder()
                .attendanceId(attendance.getAttendanceId())
                .groupId(attendance.getGroup().getGroupId())
                .userId(attendance.getUser().getUserId())
                .userName(userName)
                .sessionDate(attendance.getSessionDate())
                .isPresent(attendance.getIsPresent())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .build();
    }
}