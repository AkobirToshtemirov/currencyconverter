package com.uzum.currencyconverter.repository;

import com.uzum.currencyconverter.entity.SecurityKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityKeyRepository extends JpaRepository<SecurityKey, Long> {
    @Query(value = "SELECT key_value FROM secret_key limit 1", nativeQuery = true)
    String getSecretKey();
}
