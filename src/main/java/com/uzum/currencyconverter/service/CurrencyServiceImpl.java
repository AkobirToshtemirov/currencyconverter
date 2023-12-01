package com.uzum.currencyconverter.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uzum.currencyconverter.dto.CommissionDTO;
import com.uzum.currencyconverter.dto.CurrencyDTO;
import com.uzum.currencyconverter.entity.Commission;
import com.uzum.currencyconverter.exception.NotFoundException;
import com.uzum.currencyconverter.repository.CommissionRepository;
import com.uzum.currencyconverter.repository.SecretKeyRepository;
import com.uzum.currencyconverter.service.api.CurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CurrencyServiceImpl implements CurrencyService {
    private static final String UZS = "UZS";

    private static final Float DEFAULT_COMMISSION_PERCENTAGE = 0.0F;
    private static final String API_BASE_URL = "https://cbu.uz/ru/arkhiv-kursov-valyut/json/all/";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final CommissionRepository commissionRepository;
    private final SecretKeyRepository secretKeyRepository;

    public CurrencyServiceImpl(CommissionRepository commissionRepository, SecretKeyRepository secretKeyRepository) {
        this.commissionRepository = commissionRepository;
        this.secretKeyRepository = secretKeyRepository;
    }


    @Override
    public CommissionDTO setCommission(String secretKey, CommissionDTO commissionModel) {
        if (!validateSecretKey(secretKey)) {
            throw new SecurityException("Secret key mismatch. Access denied.");
        }

        Commission existingCommission = getCommission(commissionModel.from(), commissionModel.to());
        existingCommission.setCommissionAmount(commissionModel.commissionAmount());
        commissionRepository.save(existingCommission);

        return commissionModel;
    }


    @Override
    public void updateCurrency() {
        List<CurrencyDTO> currencies = fetchCurrencies();

        if (currencies.isEmpty()) {
            log.warn("No currency data found in the response.");
            return;
        }

        saveCommissions(currencies);
    }

    @Override
    public List<CurrencyDTO> fetchCurrencies() {
        try {
            String apiUrl = buildApiUrl();
            log.info("Fetching exchange rates from: {}", apiUrl);

            HttpResponse<String> response = sendHttpRequest(apiUrl);

            handleHttpResponse(response);

            return handleResponse(response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Failed to fetch exchange rate.", e);
        }
        return Collections.emptyList();
    }

    private Commission getCommission(String from, String to) {
        return commissionRepository.findByFromCurrencyAndToCurrency(from, to)
                .orElseThrow(() -> new NotFoundException("Commission not found for the specified currencies"));
    }

    private String buildApiUrl() {
        return API_BASE_URL + LocalDate.now() + "/";
    }

    private HttpResponse<String> sendHttpRequest(String apiUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private List<CurrencyDTO> handleResponse(String responseBody) {
        Gson gson = new Gson();
        Type currencyListType = new TypeToken<List<CurrencyDTO>>() {
        }.getType();
        return gson.fromJson(responseBody, currencyListType);
    }

    private void saveCommissions(List<CurrencyDTO> currencies) {
        currencies.forEach(currency -> {
            BigDecimal rate = new BigDecimal(currency.Rate());
            saveCommission(UZS, currency.Ccy(), DEFAULT_COMMISSION_PERCENTAGE, BigDecimal.valueOf(1.0 / rate.doubleValue()));
            saveCommission(currency.Ccy(), UZS, DEFAULT_COMMISSION_PERCENTAGE, rate);
        });
    }

    private void saveCommission(String from, String to, Float commissionAmount, BigDecimal conversionRate) {
        Optional<Commission> existingCommission = commissionRepository.findByFromCurrencyAndToCurrency(from, to);

        Commission commission;
        if (existingCommission.isPresent()) {
            commission = existingCommission.get();
            commission.setConversionRate(conversionRate);
        } else {
            commission = new Commission(from, to, commissionAmount, conversionRate);
        }
        commissionRepository.save(commission);
    }

    private boolean validateSecretKey(String inputKey) {
        return secretKeyRepository.findByKeyValue(inputKey).isPresent();
    }


    private void handleHttpResponse(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            log.warn("Failed to fetch exchange rate. HTTP Status Code: " + response.statusCode());
        }
    }
}
