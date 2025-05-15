package com.mogacko.mogacko.repository;

import com.mogacko.mogacko.entity.Meeting;
import com.mogacko.mogacko.entity.MeetingParticipant;
import com.mogacko.mogacko.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {
    Optional<MeetingParticipant> findByMeetingAndUser(Meeting meeting, User user);
    List<MeetingParticipant> findByMeeting(Meeting meeting);

    @Query("SELECT COUNT(mp) FROM MeetingParticipant mp WHERE mp.meeting = :meeting")
    int countParticipants(Meeting meeting);

    @Query("SELECT COUNT(mp) FROM MeetingParticipant mp WHERE mp.meeting = :meeting AND mp.status = 'ATTENDED'")
    int countAttendance(Meeting meeting);

    @Query("SELECT COUNT(mp) FROM MeetingParticipant mp WHERE mp.user = :user")
    int countByUser(@Param("user") User user);

    @Query("SELECT COUNT(mp) FROM MeetingParticipant mp WHERE mp.user = :user AND mp.status = 'ATTENDED'")
    int countAttendedByUser(@Param("user") User user);
}