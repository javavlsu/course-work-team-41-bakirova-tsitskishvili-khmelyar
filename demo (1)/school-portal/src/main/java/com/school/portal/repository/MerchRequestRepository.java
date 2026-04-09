package com.school.portal.repository;

import com.school.portal.model.MerchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MerchRequestRepository extends JpaRepository<MerchRequest, Integer> {
    List<MerchRequest> findByStudentUserId(Integer studentId);

    @Query("SELECT mr FROM MerchRequest mr WHERE mr.status = :status ORDER BY mr.requestDate")
    List<MerchRequest> findByStatus(@Param("status") Integer status);
}