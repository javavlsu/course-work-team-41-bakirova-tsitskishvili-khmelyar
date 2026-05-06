package com.school.portal.repository;

import com.school.portal.model.MerchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface MerchRequestRepository extends JpaRepository<MerchRequest, Integer> {
    List<MerchRequest> findByStudentUserId(Integer studentId);

    @Query("SELECT mr FROM MerchRequest mr WHERE mr.status = :status ORDER BY mr.requestDate")
    List<MerchRequest> findByStatus(@Param("status") Integer status);

    @Query("SELECT mr FROM MerchRequest mr ORDER BY CASE WHEN mr.status = 0 THEN 0 ELSE 1 END, mr.requestDate DESC")
    Page<MerchRequest> findAllWithCustomOrder(Pageable pageable);

    @Query("SELECT mr FROM MerchRequest mr WHERE " +
            "(:status IS NULL OR mr.status = :status) AND " +
            "(:search IS NULL OR :search = '' OR LOWER(CONCAT(mr.student.lastName, ' ', mr.student.firstName, ' ', COALESCE(mr.student.middleName, ''))) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY CASE WHEN mr.status = 0 THEN 0 ELSE 1 END, mr.requestDate DESC")
    Page<MerchRequest> findFilteredRequests(
            @Param("status") Integer status,
            @Param("search") String search,
            Pageable pageable);
}