package com.mogacko.mogacko.repository;

import com.mogacko.mogacko.entity.Meeting;
import com.mogacko.mogacko.entity.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByGroupOrderByMeetingDateDesc(StudyGroup group);

    @Query("SELECT m FROM Meeting m WHERE m.group = :group AND m.meetingDate < :now ORDER BY m.meetingDate DESC")
    List<Meeting> findPastMeetings(@Param("group") StudyGroup group, @Param("now") LocalDateTime now);

    @Query("SELECT m FROM Meeting m WHERE m.group = :group AND m.meetingDate > :now ORDER BY m.meetingDate ASC")
    List<Meeting> findUpcomingMeetings(@Param("group") StudyGroup group, @Param("now") LocalDateTime now);

    @Query("SELECT m FROM Meeting m WHERE m.group = :group AND m.meetingDate BETWEEN :start AND :end")
    List<Meeting> findCurrentMeetings(@Param("group") StudyGroup group, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}