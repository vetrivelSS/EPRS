// package com.scrap;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;
// import java.util.List;

// @Repository
// public interface ScrapRepository extends JpaRepository<Scrap, Long> {

//     // Used for the search bar in your UI
//     Scrap findByJobNumber(String jobNumber);

//     // Used to filter the "Available" vs "Sold" lists
//     List<Scrap> findByStatusIgnoreCase(String status);
// }
