package com.mogacko.mogacko.service;

import com.mogacko.mogacko.dto.UserStatisticsDto;
import com.mogacko.mogacko.entity.User;
import com.mogacko.mogacko.entity.UserStatistics;
import com.mogacko.mogacko.repository.GroupMemberRepository;
import com.mogacko.mogacko.repository.MeetingParticipantRepository;
import com.mogacko.mogacko.repository.UserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserStatisticsService {

    private final UserStatisticsRepository statisticsRepository;
    private final GroupMemberRepository memberRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;

    public UserStatisticsDto getUserStatistics(User user) {
        Optional<UserStatistics> statsOpt = statisticsRepository.findByUser(user);

        if (statsOpt.isPresent()) {
            UserStatistics stats = statsOpt.get();
            return mapToUserStatisticsDto(stats);
        }

        // 통계가 없으면 새로 생성
        return createUserStatistics(user);
    }

    @Transactional
    public UserStatisticsDto createUserStatistics(User user) {
        UserStatistics statistics = UserStatistics.builder()
                .user(user)
                .groupParticipationCount(0)
                .attendanceRate(0.0)
                .totalMeetings(0)
                .lastUpdated(LocalDateTime.now())
                .build();

        UserStatistics savedStats = statisticsRepository.save(statistics);
        return mapToUserStatisticsDto(savedStats);
    }

    @Transactional
    public UserStatisticsDto updateUserStatistics(User user) {
        Optional<UserStatistics> statsOpt = statisticsRepository.findByUser(user);

        UserStatistics stats;
        if (statsOpt.isPresent()) {
            stats = statsOpt.get();
        } else {
            stats = new UserStatistics();
            stats.setUser(user);
        }

        // 참여 그룹 수 계산 (활성 상태인 그룹만)
        int groupParticipationCount = memberRepository.countActiveGroupsByUser(user);
        stats.setGroupParticipationCount(groupParticipationCount);

        // 전체 참가한 모임 수 계산
        int totalParticipatedMeetings = meetingParticipantRepository.countByUser(user);

        // 출석한 모임 수 계산
        int attendedMeetings = meetingParticipantRepository.countAttendedByUser(user);

        // 출석율 계산
        double attendanceRate = totalParticipatedMeetings > 0 ?
                (double) attendedMeetings / totalParticipatedMeetings * 100 : 0;
        stats.setAttendanceRate(attendanceRate);

        // 총 모임 수
        stats.setTotalMeetings(totalParticipatedMeetings);

        // 최종 업데이트 시간
        stats.setLastUpdated(LocalDateTime.now());

        UserStatistics savedStats = statisticsRepository.save(stats);
        return mapToUserStatisticsDto(savedStats);
    }

    private UserStatisticsDto mapToUserStatisticsDto(UserStatistics stats) {
        return UserStatisticsDto.builder()
                .statId(stats.getStatId())
                .userId(stats.getUser().getUserId())
                .groupParticipationCount(stats.getGroupParticipationCount())
                .attendanceRate(stats.getAttendanceRate())
                .totalMeetings(stats.getTotalMeetings()) // 수정된 필드명
                .lastUpdated(stats.getLastUpdated())
                .build();
    }
}