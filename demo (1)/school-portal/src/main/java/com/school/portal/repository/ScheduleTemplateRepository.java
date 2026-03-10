package com.school.portal.repository;

import com.school.portal.model.ScheduleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScheduleTemplateRepository extends JpaRepository<ScheduleTemplate, Integer> {

    List<ScheduleTemplate> findBySchoolClassClassId(Integer classId);

    List<ScheduleTemplate> findByTeacherUserId(Integer teacherId);

    @Query("SELECT st FROM ScheduleTemplate st WHERE st.schoolClass.classId = :classId " +
            "AND st.dayOfWeek = :dayOfWeek ORDER BY st.lessonNumber")
    List<ScheduleTemplate> findTemplatesForClassAndDay(
            @Param("classId") Integer classId,
            @Param("dayOfWeek") Byte dayOfWeek);
}