package com.productionCreation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ProductionRepository extends JpaRepository<Production, Long> {

    // Change return type from Production to List<Production>
    int countByJobOrder(String jobOrder);

    @Query("SELECT COUNT(p) FROM Production p WHERE p.jobOrder = :jobNo")
    Long countByJobNo(@Param("jobNo") String jobNo);

}