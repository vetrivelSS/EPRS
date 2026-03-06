package com.productionCreation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductionRepository extends JpaRepository<Production, Long> {
    Production findByJobOrderNumber(String jobOrderNumber);

    Production findByProductionNumber(String productionNumber);

    List<Production> findByStatus(String status);
}
