package com.uzum.currencyconverter.runner;

import com.uzum.currencyconverter.service.api.SecretKeyService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SecretKeyInitializer implements ApplicationRunner {
    private final SecretKeyService secretKeyService;

    public SecretKeyInitializer(SecretKeyService secretKeyService) {
        this.secretKeyService = secretKeyService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        secretKeyService.initializeSecretKey();
    }
}
