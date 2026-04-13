package com.school.portal.repository;

import com.school.portal.model.ClassSubjectTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassSubjectTeacherRepository extends JpaRepository<ClassSubjectTeacher, Integer> {

    List<ClassSubjectTeacher> findBySchoolClass_ClassId(Integer classId);

    List<ClassSubjectTeacher> findByTeacher_UserId(Integer teacherId);

    Optional<ClassSubjectTeacher> findBySchoolClass_ClassIdAndSubjectSubjectId(
            Integer classId, Integer subjectId);

    @Query("SELECT DISTINCT cst.schoolClass FROM ClassSubjectTeacher cst WHERE cst.teacher.userId = :teacherId")
    List<Object> findClassesByTeacherId(@Param("teacherId") Integer teacherId);
}