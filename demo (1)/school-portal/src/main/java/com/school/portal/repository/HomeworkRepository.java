package com.school.portal.repository;

import com.school.portal.model.Homework;
import org.springframework.data.domain.Page; // Для пагинации на уровне БД
import org.springframework.data.domain.Pageable; // Для пагинации на уровне БД
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import java.time.LocalDateTime;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Integer> {

    List<Homework> findByStudentUserId(Integer studentId);

    List<Homework> findByLessonLessonId(Integer lessonId);

    Optional<Homework> findByStudentUserIdAndLessonLessonId(Integer studentId, Integer lessonId);

    @Query("SELECT h FROM Homework h WHERE h.lesson.schoolClass.classId = :classId " +
            "AND h.status = :status ORDER BY h.date DESC")
    List<Homework> findHomeworkByClassAndStatus(
            @Param("classId") Integer classId,
            @Param("status") Integer status);

    @Query("SELECT h FROM Homework h WHERE h.lesson.teacher.userId = :teacherId " +
            "ORDER BY h.date DESC")
    List<Homework> findHomeworkForTeacher(@Param("teacherId") Integer teacherId);

    @Query("SELECT h FROM Homework h WHERE h.status = :status ORDER BY h.date DESC")
    List<Homework> findByStatus(@Param("status") Integer status);

    // МЕТОД ДЛЯ ПАГИНАЦИИ И ФИЛЬТРАЦИИ НА УРОВНЕ БД
    @Query("SELECT h FROM Homework h " +
            "JOIN h.lesson l " +
            "JOIN h.student s " +
            "WHERE h.status = :status " +
            "AND l.schoolClass.classId = :classId " +
            "AND (:subjectId = 0 OR l.subject.subjectId = :subjectId) " +
            "AND (:teacherId IS NULL OR l.teacher.userId = :teacherId) " +
            "AND (:search IS NULL OR :search = '' OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:dateFrom IS NULL OR h.date >= :dateFrom) " +
            "AND (:dateTo IS NULL OR h.date <= :dateTo)")
    Page<Homework> findFilteredHomeworks(
            @Param("status") Integer status,
            @Param("classId") Integer classId,
            @Param("subjectId") Integer subjectId,
            @Param("teacherId") Integer teacherId,
            @Param("search") String search,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );
}