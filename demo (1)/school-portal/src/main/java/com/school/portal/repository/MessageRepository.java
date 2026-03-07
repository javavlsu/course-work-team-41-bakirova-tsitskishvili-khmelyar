package com.school.portal.repository;

import com.school.portal.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("SELECT m FROM Message m WHERE m.toUser.userId = :userId " +
            "AND m.status != 2 ORDER BY m.sentAt DESC")
    List<Message> findInboxMessages(@Param("userId") Integer userId);

    @Query("SELECT m FROM Message m WHERE m.fromUser.userId = :userId " +
            "ORDER BY m.sentAt DESC")
    List<Message> findSentMessages(@Param("userId") Integer userId);

    @Query("SELECT m FROM Message m WHERE m.toUser.userId = :userId " +
            "AND m.status = 2 ORDER BY m.sentAt DESC")
    List<Message> findArchivedMessages(@Param("userId") Integer userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.toUser.userId = :userId AND m.status = 0")
    Long countUnreadMessages(@Param("userId") Integer userId);
}