package com.uzum.currencyconverter.repository;

import com.uzum.currencyconverter.entity.Commission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {
    Optional<Commission> findByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);
}
