package com.school.portal.repository;

import com.school.portal.model.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Integer> {

    List<TransactionHistory> findByStudentUserId(Integer studentId);

    @Query("SELECT SUM(t.amount) FROM TransactionHistory t WHERE t.student.userId = :studentId")
    Integer getTotalCoinsForStudent(@Param("studentId") Integer studentId);

    @Query("SELECT t FROM TransactionHistory t WHERE t.student.userId = :studentId ORDER BY t.date DESC")
    List<TransactionHistory> findLatestTransactions(@Param("studentId") Integer studentId);
}