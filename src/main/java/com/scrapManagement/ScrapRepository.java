package com.ScrapManagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapRepository extends JpaRepository<ScrapRecord, Long> {
    // Standard CRUD operations are inherited automatically
}
