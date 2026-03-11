package com.productionCreation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionRepository extends JpaRepository<Production, Long> {
    // Production findByJobOrderNumber(String jobOrderNumber);
    // Optional<Production> findByJobOrderNumber(String jobOrderNumber);

    // FIX: Must be Uppercase 'P' and return Optional
    Optional<Production> findByProductionNumber(String productionNumber);

    @Query("SELECT p FROM Production p WHERE p.status = :status")
    List<Production> findProductionByStatus(@Param("status") String status);

    @Query(value = "SELECT p.* FROM production p INNER JOIN job_orders j ON p.job_order_number = j.job_order_number WHERE p.status = :status", nativeQuery = true)
    List<Production> findByStatusWithNativeJoin(@Param("status") String status);

    // Production findByJobOrderNumber(String jobOrder);

    // Optional<Production> findByProductionNumber(String productionNumber);
    int countByJobOrder(String jobOrder);

    @Query("SELECT COUNT(p) FROM Production p WHERE p.jobOrder = :jobNo")
    Long countByJobNo(@Param("jobNo") String jobNo);

    // ProductionRepository.java

    List<Production> findByJobOrder(String jobOrder);

    @Query("SELECT p FROM Production p WHERE p.jobOrder = :jobOrder")
    List<Production> findByJobOrderNumber(@Param("jobOrder") String jobOrder);

    // @Query("SELECT p FROM Production p WHERE p.jobOrder = :jobOrder")
    // List<Production> findByJobOrderNumber(@Param("jobOrder") String jobOrder);

}
