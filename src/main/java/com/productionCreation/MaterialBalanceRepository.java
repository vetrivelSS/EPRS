package com.productionCreation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaterialBalanceRepository extends JpaRepository<MaterialBalance, Long> {

    List<MaterialBalance> findByProductionNumber(String productionNumber);

    List<MaterialBalance> findByScrapType(String scrapType);

    MaterialBalance findFirstByProductionNumberOrderByIdDesc(String productionNumber);
}
