package com.school.portal.repository;

import com.school.portal.model.Announcement;
import com.school.portal.model.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

    List<Announcement> findBySchoolClassOrderByCreatedAtDesc(SchoolClass schoolClass);

    @Query("SELECT a FROM Announcement a WHERE a.schoolClass IS NULL ORDER BY a.createdAt DESC")
    List<Announcement> findGlobalAnnouncements();

    @Query("SELECT a FROM Announcement a WHERE a.schoolClass = :class OR a.schoolClass IS NULL ORDER BY a.createdAt DESC")
    List<Announcement> findAnnouncementsForClass(@Param("class") SchoolClass schoolClass);

    @Query(" SELECT a FROM Announcement a WHERE a.schoolClass = :schoolClass AND a.createdAt >= :date ORDER BY a.createdAt DESC ")
    List<Announcement> findRecentAnnouncementsForClass(
            @Param("schoolClass") SchoolClass schoolClass,
            @Param("date") LocalDateTime date
    );
}
