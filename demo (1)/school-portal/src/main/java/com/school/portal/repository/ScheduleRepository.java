package com.school.portal.repository;

import com.school.portal.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    List<Schedule> findBySchoolClassClassIdOrderByLessonDateTime(Integer classId);

    List<Schedule> findByTeacherUserIdOrderByLessonDateTime(Integer teacherId);

    @Query("SELECT s FROM Schedule s WHERE s.schoolClass.classId = :classId " +
            "AND s.lessonDateTime BETWEEN :startDate AND :endDate ORDER BY s.lessonDateTime")
    List<Schedule> findLessonsForClassBetween(
            @Param("classId") Integer classId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Schedule s WHERE s.teacher.userId = :teacherId " +
            "AND s.lessonDateTime BETWEEN :startDate AND :endDate ORDER BY s.lessonDateTime")
    List<Schedule> findLessonsForTeacherBetween(
            @Param("teacherId") Integer teacherId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Schedule s WHERE s.lessonDateTime BETWEEN :startDate AND :endDate ORDER BY s.lessonDateTime")
    List<Schedule> findLessonsBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}