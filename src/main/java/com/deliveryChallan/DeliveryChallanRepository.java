package com.deliveryChallan;
import org.springframework.data.jpa.repository.JpaRepository;


	public interface DeliveryChallanRepository extends JpaRepository<DeliveryChallan, Long> {
	    DeliveryChallan findByJobOrderNumber(String jobOrderNumber);
	}
