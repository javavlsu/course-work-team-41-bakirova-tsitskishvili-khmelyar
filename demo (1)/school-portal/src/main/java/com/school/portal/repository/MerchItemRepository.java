package com.school.portal.repository;

import com.school.portal.model.MerchItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MerchItemRepository extends JpaRepository<MerchItem, Integer> {
    List<MerchItem> findByOrderByPriceAsc();
    List<MerchItem> findByPriceLessThanEqual(Integer maxPrice);
    List<MerchItem> findByIsArchivedFalseOrderByPriceAsc();
}