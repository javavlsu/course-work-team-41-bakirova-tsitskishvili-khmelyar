package com.school.portal.repository;

import com.school.portal.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    List<Attendance> findByStudentUserId(Integer studentId);

    List<Attendance> findByLessonLessonId(Integer lessonId);

    Optional<Attendance> findByStudentUserIdAndLessonLessonId(Integer studentId, Integer lessonId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.userId = :studentId " +
            "AND a.lesson.lessonDateTime BETWEEN :startDate AND :endDate")
    Long countAbsencesForStudentInPeriod(
            @Param("studentId") Integer studentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}