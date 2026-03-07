package com.school.portal.repository;

import com.school.portal.model.StudentClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentClassRepository extends JpaRepository<StudentClass, Integer> {

    List<StudentClass> findBySchoolClassClassId(Integer classId);

    Optional<StudentClass> findByStudentUserId(Integer studentId);

    @Query("SELECT sc.student FROM StudentClass sc WHERE sc.schoolClass.classId = :classId")
    List<Object> findStudentsByClassId(@Param("classId") Integer classId);
}