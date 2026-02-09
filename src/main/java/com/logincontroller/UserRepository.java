package com.logincontroller;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Email illana Phone vachi user ah find pannum
    Optional<User> findByEmailOrPhone(String email, String phone);
    Optional<User> findByEmail(String email);

    // Neenga munnadiye ezhuthuna method
}
