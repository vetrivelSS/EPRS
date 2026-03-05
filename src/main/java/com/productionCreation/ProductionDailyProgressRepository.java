package com.productionCreation;

import com.productionCreation.ProductionDailyProgress; // Import your entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductionDailyProgressRepository extends JpaRepository<ProductionDailyProgress, Long> {
    // This is the method required to show ID 1, 2, and 3 together
    // ADD THIS LINE: This fixes the "undefined" error
    List<ProductionDailyProgress> findByProductionNumber(String productionNumber);

}