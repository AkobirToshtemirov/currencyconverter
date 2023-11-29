package com.uzum.currencyconverter.repository;

import com.uzum.currencyconverter.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account getAccountByCurrencyName(String currencyName);
}
