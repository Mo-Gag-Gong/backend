package com.mogacko.mogacko.repository;

import com.mogacko.mogacko.entity.Attendance;
import com.mogacko.mogacko.entity.StudyGroup;
import com.mogacko.mogacko.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByGroupAndSessionDate(StudyGroup group, LocalDate sessionDate);

    Optional<Attendance> findByGroupAndUserAndSessionDate(StudyGroup group, User user, LocalDate sessionDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.user = :user AND a.isPresent = true")
    int countUserAttendance(User user);

    @Query("SELECT a.sessionDate, COUNT(a) FROM Attendance a WHERE a.group = :group AND a.isPresent = true GROUP BY a.sessionDate")
    List<Object[]> getGroupAttendanceStats(StudyGroup group);
}