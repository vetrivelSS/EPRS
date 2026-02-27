package com.jobOrderCreation;

	import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import jakarta.transaction.Transactional;
	
		@Repository
		public interface JobOrderRepository extends JpaRepository<JobOrder, Long> {
		    List<JobOrder> findByStatus(String status);
		    @Modifying
		    @Transactional
		    @Query(value = "UPDATE job_orders SET status = :status WHERE job_order_number = :jobNo", nativeQuery = true)
		    void updateStatusByJobNumber(@Param("jobNo") String jobNo, @Param("status") String status);
		    JobOrder findByJobOrderNumber(String jobOrderNumber);
		    boolean existsByJobOrderNumber(String jobOrderNumber);
		}
	

	 