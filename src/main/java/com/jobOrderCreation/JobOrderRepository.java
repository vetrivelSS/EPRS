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
			
			    
//			List<JobOrder> findByStatus(String status);
//		    JobOrder findByJobOrderNumber(String jobOrderNumber);
//		    @Modifying
//		    @Transactional
//		    @Query(value = "UPDATE job_orders SET status = :status WHERE job_order_number = :jobNo", nativeQuery = true)
//		    void updateStatusByJobNumber(@Param("jobNo") String jobNo, @Param("status") String status);
//		}
			 // Join Query: Fetch Job Order details where status is PENDING

			    // 1. Fixes your error: This allows Spring to find by status string
			    List<JobOrder> findByStatus(String status);

			    // 2. Your specific request: Fetch Job Orders using a Join with Production
			    @Query(value = "SELECT j.* FROM job_orders j " +
			                   "LEFT JOIN production p ON j.job_order_number = p.job_order_number " +
			                   "WHERE j.status = :status", nativeQuery = true)
			    List<JobOrder> findPendingJobsWithJoin(@Param("status") String status);

			    // 3. Helper to find a specific job by number
			    JobOrder findByJobOrderNumber(String jobOrderNumber);

			    @Modifying
			    @Transactional
			    @Query(value = "UPDATE job_orders SET status = :status WHERE job_order_number = :jobNo", nativeQuery = true)
			    void updateStatusByJobNumber(@Param("jobNo") String jobNo, @Param("status") String status);
			}
		
	
 