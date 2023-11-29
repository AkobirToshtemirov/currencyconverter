package com.uzum.currencyconverter.service;

import com.uzum.currencyconverter.dto.CurrencyDTO;
import com.uzum.currencyconverter.entity.Account;
import com.uzum.currencyconverter.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService{
    private final DecimalFormat decimalFormat = new DecimalFormat("#.######");
    private final AccountRepository accountRepository;
    private final CurrencyService currencyService;

    public AccountServiceImpl(AccountRepository accountRepository, CurrencyService currencyService) {
        this.accountRepository = accountRepository;
        this.currencyService = currencyService;
    }

    public void initializeAccounts() {
        createOrUpdateAccount("UZS", generateRandomAmount());
        List<CurrencyDTO> currencies = currencyService.fetchCurrencies();
        currencies.forEach(currency -> createOrUpdateAccount(currency.Ccy(), generateRandomAmount()));
    }

    private void createOrUpdateAccount(String currencyName, Double amount) {
        Optional<Account> existingAccount = accountRepository.getAccountByCurrencyName(currencyName);

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

    private Double generateRandomAmount() {
        double randomValue = Math.random() * (50000 - 500) + 500;
        return Double.parseDouble(decimalFormat.format(randomValue));
    }
}
