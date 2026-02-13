package com.jobOrderCreation;

	import org.springframework.data.jpa.repository.JpaRepository;
	import org.springframework.stereotype.Repository;

	@Repository
	public interface JobOrderRepository extends JpaRepository<JobOrder, Long> {
	    // You can add custom searches here later
	}

