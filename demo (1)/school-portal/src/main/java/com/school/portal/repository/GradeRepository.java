package com.school.portal.repository;

import com.school.portal.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Integer> {

    List<Grade> findByStudentUserId(Integer studentId);

    List<Grade> findByLessonLessonId(Integer lessonId);

    Optional<Grade> findByStudentUserIdAndLessonLessonId(Integer studentId, Integer lessonId);

    @Query("SELECT AVG(g.gradeValue) FROM Grade g WHERE g.student.userId = :studentId " +
            "AND g.gradeValue IS NOT NULL")
    Double getAverageGradeForStudent(@Param("studentId") Integer studentId);

    @Query("SELECT g FROM Grade g WHERE g.student.userId = :studentId " +
            "AND g.lesson.subject.subjectId = :subjectId " +
            "AND g.lesson.lessonDateTime BETWEEN :startDate AND :endDate")
    List<Grade> findGradesForStudentBySubjectAndPeriod(
            @Param("studentId") Integer studentId,
            @Param("subjectId") Integer subjectId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT g FROM Grade g WHERE g.lesson.lessonId = :lessonId")
    List<Grade> findGradesForLesson(@Param("lessonId") Integer lessonId);
}