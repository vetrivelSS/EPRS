package com.productionCreation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductionResultRepository extends JpaRepository<ProductionResult, Integer> {
    List<ProductionResult> findByScrapType(String scrapType);

    Production findByProductionNumber(String productionNumber);

    int countByProductionNumber(String productionNumber);
}