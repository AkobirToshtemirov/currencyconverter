package com.uzum.currencyconverter.repository;

import com.uzum.currencyconverter.entity.SecretKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecretKeyRepository extends JpaRepository<SecretKey, Long> {
    Optional<SecretKey> findByKeyValue(String secretKey);
}
