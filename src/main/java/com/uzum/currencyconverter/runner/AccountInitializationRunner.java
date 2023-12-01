package com.uzum.currencyconverter.runner;

import com.uzum.currencyconverter.service.api.AccountService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AccountInitializationRunner implements ApplicationRunner {
    private final AccountService accountService;

    public AccountInitializationRunner(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        accountService.initializeAccounts();
    }
}
