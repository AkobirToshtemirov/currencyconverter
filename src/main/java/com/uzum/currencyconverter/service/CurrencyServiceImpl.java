package com.uzum.currencyconverter.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uzum.currencyconverter.dto.CurrencyDTO;
import com.uzum.currencyconverter.entity.Commission;
import com.uzum.currencyconverter.exception.ExchangeRateFetchException;
import com.uzum.currencyconverter.exception.NoCurrencyDataException;
import com.uzum.currencyconverter.repository.CommissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CurrencyServiceImpl implements CurrencyService {
    private static final Float DEFAULT_COMMISSION_PERCENTAGE = 0.0F;
    private static final String API_BASE_URL = "https://cbu.uz/ru/arkhiv-kursov-valyut/json/all/";

    private final CommissionRepository commissionRepository;

    public CurrencyServiceImpl(CommissionRepository commissionRepository) {
        this.commissionRepository = commissionRepository;
    }

    @Override
    public void updateCurrency() {

        List<CurrencyDTO> currencies = fetchCurrencies();

        if (currencies.isEmpty())
            throw new NoCurrencyDataException("No currency data found in the response.");

        saveCommissions(currencies);

    }

    private String buildApiUrl() {
        return API_BASE_URL + LocalDate.now() + "/";
    }

    private List<CurrencyDTO> parseResponse(String responseBody) {
        Gson gson = new Gson();
        Type currencyListType = new TypeToken<List<CurrencyDTO>>() {
        }.getType();
        return gson.fromJson(responseBody, currencyListType);
    }

    private void saveCommissions(List<CurrencyDTO> currencies) {
        currencies.forEach(currency -> {
            saveCommission("UZS", currency.Ccy(), DEFAULT_COMMISSION_PERCENTAGE, 1.0 / Double.parseDouble(currency.Rate()));
            saveCommission(currency.Ccy(), "UZS", DEFAULT_COMMISSION_PERCENTAGE, Double.parseDouble(currency.Rate()));
        });
    }

    private void saveCommission(String from, String to, Float commissionAmount, Double conversionRate) {
        Optional<Commission> existingCommission = commissionRepository.getCommissionByFromCurrencyAndToCurrency(from, to);

        Commission commission;
        if (existingCommission.isPresent()) {
            commission = existingCommission.get();
            commission.setConversionRate(conversionRate);
        } else {
            commission = new Commission(from, to, commissionAmount, conversionRate);
        }
        commissionRepository.save(commission);
    }


    @Override
    public List<CurrencyDTO> fetchCurrencies() {
        try {
            String apiUrl = buildApiUrl();
            log.info("Fetching exchange rates from: {}", apiUrl);

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
                throw new ExchangeRateFetchException("Failed to fetch exchange rate. HTTP Status Code: " + response.statusCode());

            return parseResponse(response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExchangeRateFetchException("Failed to fetch exchange rate.");
        }
    }
}
