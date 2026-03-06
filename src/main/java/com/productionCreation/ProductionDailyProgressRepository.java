package com.productionCreation;

// import com.productionCreation.ProductionDailyProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductionDailyProgressRepository extends JpaRepository<ProductionDailyProgress, Long> {
    List<ProductionDailyProgress> findByProductionNumber(String productionNumber);

}