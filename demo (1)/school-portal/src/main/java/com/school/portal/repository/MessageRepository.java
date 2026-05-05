package com.school.portal.repository;

import com.school.portal.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("SELECT m FROM Message m WHERE m.toUser.userId = :userId " +
            "AND (:search IS NULL OR LOWER(m.fromUser.lastName || ' ' || m.fromUser.firstName || ' ' || m.fromUser.middleName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY m.sentAt DESC")
    Page<Message> findInboxMessagesWithSearch(@Param("userId") Integer userId, @Param("search") String search, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.fromUser.userId = :userId " +
            "AND (:search IS NULL OR LOWER(m.toUser.lastName || ' ' || m.toUser.firstName || ' ' || m.toUser.middleName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY m.sentAt DESC")
    Page<Message> findSentMessagesWithSearch(@Param("userId") Integer userId, @Param("search") String search, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE (m.toUser.userId = :userId OR m.fromUser.userId = :userId) " +
            "AND m.status = com.school.portal.model.enums.MessageStatus.ELECT " + // Указан полный путь к Enum
            "AND (:search IS NULL OR LOWER(m.fromUser.lastName || ' ' || m.fromUser.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(m.toUser.lastName || ' ' || m.toUser.firstName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY m.sentAt DESC")
    Page<Message> findElectMessagesWithSearch(@Param("userId") Integer userId, @Param("search") String search, Pageable pageable);


    @Query("SELECT COUNT(m) FROM Message m WHERE m.toUser.userId = :userId AND m.status = com.school.portal.model.enums.MessageStatus.NEW")
    Long countUnreadMessages(@Param("userId") Integer userId);

    @Query("SELECT m FROM Message m WHERE m.toUser.userId = :userId ORDER BY m.sentAt DESC")
    Page<Message> findInboxMessages(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.fromUser.userId = :userId ORDER BY m.sentAt DESC")
    Page<Message> findSentMessages(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE (m.toUser.userId = :userId OR m.fromUser.userId = :userId) " +
            "AND m.status = com.school.portal.model.enums.MessageStatus.ELECT ORDER BY m.sentAt DESC")
    Page<Message> findElectMessages(@Param("userId") Integer userId, Pageable pageable);
}