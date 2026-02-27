package com.productionCreation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductionRepository extends JpaRepository<Production, Long> {

	 List<Production> findByStatus(String status);

	    @Query("SELECT p FROM Production p WHERE p.status = :status")
	    List<Production> findProductionByStatus(@Param("status") String status);

	    @Query(value = "SELECT p.* FROM production p INNER JOIN job_orders j ON p.job_order_number = j.job_order_number WHERE p.status = :status", nativeQuery = true)
	    List<Production> findByStatusWithNativeJoin(@Param("status") String status);
	    Production findByJobOrderNumber(String jobOrderNumber);
	    
	    
}
