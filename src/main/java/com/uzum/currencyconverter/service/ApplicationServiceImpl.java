package com.uzum.currencyconverter.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uzum.currencyconverter.dto.CommissionDTO;
import com.uzum.currencyconverter.dto.ConversionDTO;
import com.uzum.currencyconverter.dto.CurrencyDTO;
import com.uzum.currencyconverter.dto.RateDTO;
import com.uzum.currencyconverter.entity.Commission;
import com.uzum.currencyconverter.exception.InvalidPairException;
import com.uzum.currencyconverter.exception.NotFoundException;
import com.uzum.currencyconverter.exception.OfficialRateFetchException;
import com.uzum.currencyconverter.repository.AccountRepository;
import com.uzum.currencyconverter.repository.CommissionRepository;
import com.uzum.currencyconverter.repository.SecurityKeyRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    private static final String DECIMAL_PATTERN = "#.######";
    private static final String API_BASE_URL = "https://cbu.uz/ru/arkhiv-kursov-valyut/json/";

    private final AccountRepository accountRepository;
    private final CommissionRepository commissionRepository;
    private final SecurityKeyRepository securityKeyRepository;

    public ApplicationServiceImpl(AccountRepository accountRepository, CommissionRepository commissionRepository, SecurityKeyRepository securityKeyRepository) {
        this.accountRepository = accountRepository;
        this.commissionRepository = commissionRepository;
        this.securityKeyRepository = securityKeyRepository;
    }

    @Override
    public ConversionDTO getConversion(String from, String to, Double amount) {
        Double resultAmount;
        if (from.equals(to))
            throw new InvalidPairException("Conversion between same currency is not allowed!");

        if (from.equals("UZS") || to.equals("UZS")) {
            Optional<Commission> commission = commissionRepository.getCommissionByFromCurrencyAndToCurrency(from, to);
            if (commission.isEmpty())
                throw new NotFoundException("This pair does not exists!");

            resultAmount = calculate(commission.get(), amount);
        } else {
            Optional<Commission> commission = commissionRepository.getCommissionByFromCurrencyAndToCurrency(from, "UZS");
            if (commission.isEmpty())
                throw new NotFoundException("This pair does not exists!");

            resultAmount = calculate(commission.get(), amount);
            commission = commissionRepository.getCommissionByFromCurrencyAndToCurrency("UZS", to);
            if (commission.isEmpty())
                throw new NotFoundException("This pair does not exists!");

            resultAmount = calculate(commission.get(), resultAmount);
        }

        return new ConversionDTO(from, to, resultAmount);
    }

    @Override
    public RateDTO getOfficialRate(String date, String pair) {
        String[] currencies = pair.split("/");

        if (currencies.length != 2)
            throw new InvalidPairException("Pair is invalid!");

        if (!currencies[0].equals("UZS") && !currencies[1].equals("UZS")) {
            // UZS is not included
            return null;
        }

        try {
            String apiUrl = buildApiUrl(currencies[0].equals("UZS") ? currencies[1] : currencies[0], date);
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
                throw new OfficialRateFetchException("Failed to fetch official exchange rate. HTTP Status Code: " + response.statusCode());

            Gson gson = new Gson();
            Type currencyListType = new TypeToken<List<CurrencyDTO>>() {
            }.getType();
            List<CurrencyDTO> currenciesList = gson.fromJson(response.body(), currencyListType);

            BigDecimal rate = getRateForCurrencyPair(currencies[0], currencies[1], currenciesList);

            return new RateDTO(currencies[0], currencies[1], rate, LocalDate.parse(date));

        } catch (IOException | InterruptedException | OfficialRateFetchException e) {
            throw new OfficialRateFetchException("Failed to fetch official exchange rate.");
        }
    }

    @Override
    public ConversionDTO performConversion(ConversionDTO conversionDTO) {
        return null;
    }

    @Override
    public CommissionDTO setCommission(String secretKey, CommissionDTO commissionDTO) {
        return null;
    }

    private String buildApiUrl(String currency, String date) {
        return API_BASE_URL + currency + "/" + date + "/";
    }

    private BigDecimal getRateForCurrencyPair(String from, String to, List<CurrencyDTO> currenciesList) {
//        DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_PATTERN);

        if (from.equals("UZS"))
            return BigDecimal.valueOf(1.0 / Double.parseDouble(currenciesList.get(0).Rate()));
        else if (to.equals("UZS"))
            return BigDecimal.valueOf(Double.parseDouble(currenciesList.get(0).Rate()));
        else
            return BigDecimal.valueOf(Double.parseDouble(currenciesList.get(0).Rate()));
    }

    private Double calculate(Commission commission, Double amount) {
        return (((100 - commission.getCommissionAmount()) * amount) / 100) * commission.getConversionRate();
    }
}
