package com.DeliveryChallan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DeliveryChallanRepository extends JpaRepository<DeliveryChallan, Long> {
    // Custom queries venum-na inga add pannalam
    // Example: Optional<DeliveryChallan> findByChallanNumber(String challanNumber);
    // Optional<DeliveryChallan> findByChallanNumber(String challanNumber);
    Optional<DeliveryChallan> findByChallanNumber(String challanNumber);

}