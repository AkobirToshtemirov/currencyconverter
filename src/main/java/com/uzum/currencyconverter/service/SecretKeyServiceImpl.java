package com.uzum.currencyconverter.service;

import com.uzum.currencyconverter.entity.SecretKey;
import com.uzum.currencyconverter.repository.SecretKeyRepository;
import com.uzum.currencyconverter.service.api.SecretKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class SecretKeyServiceImpl implements SecretKeyService {
    private final SecretKeyRepository secretKeyRepository;

    @Value("${secret.key.file.path}")
    private String secretKeyFilePath;

    public SecretKeyServiceImpl(SecretKeyRepository secretKeyRepository) {
        this.secretKeyRepository = secretKeyRepository;
    }

    @Override
    public void initializeSecretKey() {
        try {
            String secretKeyValue = readSecretKeyFromFile();
            saveSecretKeyToDatabase(secretKeyValue);
        } catch (IOException e) {
            log.warn("Failed to read the secret key file.", e);
        }
    }


    private String readSecretKeyFromFile() throws IOException {
        ClassPathResource resource = new ClassPathResource(secretKeyFilePath);
        return new String(resource.getInputStream().readAllBytes());
    }

    private void saveSecretKeyToDatabase(String secretKeyValue) {
        if (secretKeyRepository.findByKeyValue(secretKeyValue).isEmpty()) {
            SecretKey secretKey = new SecretKey();
            secretKey.setKeyValue(secretKeyValue);
            secretKeyRepository.save(secretKey);
        }
    }
}
