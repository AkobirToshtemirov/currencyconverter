package com.uzum.currencyconverter.service;

import com.uzum.currencyconverter.dto.CurrencyDTO;
import com.uzum.currencyconverter.entity.Account;
import com.uzum.currencyconverter.repository.AccountRepository;
import com.uzum.currencyconverter.service.api.AccountService;
import com.uzum.currencyconverter.service.api.CurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final DecimalFormat decimalFormat = new DecimalFormat("#.######");
    private final AccountRepository accountRepository;
    private final CurrencyService currencyService;

    public AccountServiceImpl(AccountRepository accountRepository, CurrencyService currencyService) {
        this.accountRepository = accountRepository;
        this.currencyService = currencyService;
    }

    @Override
    public void initializeAccounts() {
        createOrUpdateAccount("UZS", generateRandomAmount());
        List<CurrencyDTO> currencies = currencyService.fetchCurrencies();
        currencies.forEach(currency -> createOrUpdateAccount(currency.Ccy(), generateRandomAmount()));
    }

    private void createOrUpdateAccount(String currencyName, BigDecimal amount) {
        Optional<Account> existingAccount = accountRepository.findByCurrencyName(currencyName);

        Account account;
        if (existingAccount.isPresent()) {
            account = existingAccount.get();
            account.setAmount(amount);
        } else {
            account = new Account();
            account.setCurrencyName(currencyName);
            account.setAmount(amount);
        }
        accountRepository.save(account);
    }

    private BigDecimal generateRandomAmount() {
        double randomValue = Math.random() * (50000 - 500) + 500;
        return BigDecimal.valueOf(Double.parseDouble(decimalFormat.format(randomValue)));
    }
}
