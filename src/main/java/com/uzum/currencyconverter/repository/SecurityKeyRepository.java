package com.uzum.currencyconverter.repository;

import com.uzum.currencyconverter.entity.SecurityKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityKeyRepository extends JpaRepository<SecurityKey, Long> {
}
