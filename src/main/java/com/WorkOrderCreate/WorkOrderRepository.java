package com.WorkOrderCreate;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    
    // Work Order Number (WON) vechu search panna intha method use aagum
    Optional<WorkOrder> findByWorkOrderNumber(String workOrderNumber);
    

    // Oru customer-oda ella orders-aiyum yedukka:
    // List<WorkOrder> findByCustomer(String customer);
}