package com.school.portal.repository;

import com.school.portal.model.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Integer> {

    List<Homework> findByStudentUserId(Integer studentId);

    List<Homework> findByLessonLessonId(Integer lessonId);

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
}