package com.uzum.currencyconverter.runner;

import com.uzum.currencyconverter.service.api.CurrencyService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CurrencyUpdateRunner implements ApplicationRunner {
    private final CurrencyService currencyService;

    public CurrencyUpdateRunner(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        currencyService.updateCurrency();
    }
}
