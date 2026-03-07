package com.school.portal.repository;

import com.school.portal.model.StudentParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentParentRepository extends JpaRepository<StudentParent, Integer> {

    List<StudentParent> findByStudentUserId(Integer studentId);

    List<StudentParent> findByParentUserId(Integer parentId);

    @Query("SELECT sp.student FROM StudentParent sp WHERE sp.parent.userId = :parentId")
    List<Object> findStudentsByParentId(@Param("parentId") Integer parentId);

    @Query("SELECT sp.parent FROM StudentParent sp WHERE sp.student.userId = :studentId")
    List<Object> findParentsByStudentId(@Param("studentId") Integer studentId);
}