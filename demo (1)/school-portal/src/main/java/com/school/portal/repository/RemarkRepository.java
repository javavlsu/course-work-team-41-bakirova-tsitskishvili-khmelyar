package com.school.portal.repository;

import com.school.portal.model.Remark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RemarkRepository extends JpaRepository<Remark, Integer> {
    List<Remark> findByStudentUserId(Integer studentId);
    List<Remark> findByLessonLessonId(Integer lessonId);
}