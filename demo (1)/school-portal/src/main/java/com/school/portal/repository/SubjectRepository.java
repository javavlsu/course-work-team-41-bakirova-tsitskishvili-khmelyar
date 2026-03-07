package com.school.portal.repository;

import com.school.portal.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {

    Optional<Subject> findBySubjectName(String subjectName);

    @Query("SELECT s FROM Subject s WHERE s.subjectId IN " +
            "(SELECT cst.subject.subjectId FROM ClassSubjectTeacher cst WHERE cst.teacher.userId = :teacherId)")
    List<Subject> findSubjectsByTeacherId(@Param("teacherId") Integer teacherId);

    @Query("SELECT s FROM Subject s WHERE s.subjectId IN " +
            "(SELECT cst.subject.subjectId FROM ClassSubjectTeacher cst WHERE cst.schoolClass.classId = :classId)")
    List<Subject> findSubjectsByClassId(@Param("classId") Integer classId);
}