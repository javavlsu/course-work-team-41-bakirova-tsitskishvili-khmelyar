package com.school.portal.repository;

import com.school.portal.model.SchoolClass;
import com.school.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Integer> {

    Optional<SchoolClass> findByClassNumberAndClassLetter(Integer classNumber, String classLetter);

    List<SchoolClass> findByClassTeacher(User classTeacher);

    @Query("SELECT sc FROM SchoolClass sc WHERE sc.classId IN " +
            "(SELECT sc2.schoolClass.classId FROM StudentClass sc2 WHERE sc2.student.userId = :studentId)")
    Optional<SchoolClass> findClassByStudentId(@Param("studentId") Integer studentId);

    @Query("SELECT sc FROM SchoolClass sc WHERE sc.classId IN " +
            "(SELECT cst.schoolClass.classId FROM ClassSubjectTeacher cst WHERE cst.teacher.userId = :teacherId)")
    List<SchoolClass> findClassesByTeacherId(@Param("teacherId") Integer teacherId);
}