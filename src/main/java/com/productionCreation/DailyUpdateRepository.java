package com.productionCreation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyUpdateRepository extends JpaRepository<DailyProductionUpdate, Long> {
    // String productionId-ai vachu list edukka
    List<DailyProductionUpdate> findByProductionIdOrderByEntryTimestampDesc(String productionId);
}