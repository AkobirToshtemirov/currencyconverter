package com.uzum.currencyconverter.repository;

import com.uzum.currencyconverter.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> getAccountByCurrencyName(String currencyName);
}
