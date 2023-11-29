package com.uzum.currencyconverter.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uzum.currencyconverter.dto.CommissionDTO;
import com.uzum.currencyconverter.dto.ConversionDTO;
import com.uzum.currencyconverter.dto.CurrencyDTO;
import com.uzum.currencyconverter.dto.RateDTO;
import com.uzum.currencyconverter.entity.Account;
import com.uzum.currencyconverter.entity.Commission;
import com.uzum.currencyconverter.exception.InvalidPairException;
import com.uzum.currencyconverter.exception.NotFoundException;
import com.uzum.currencyconverter.exception.OfficialRateFetchException;
import com.uzum.currencyconverter.mapper.CommissionDTOMapper;
import com.uzum.currencyconverter.repository.AccountRepository;
import com.uzum.currencyconverter.repository.CommissionRepository;
import com.uzum.currencyconverter.repository.SecurityKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {
    private static final String DECIMAL_PATTERN = "#.######";
    private static final String API_BASE_URL = "https://cbu.uz/ru/arkhiv-kursov-valyut/json/";
    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat(DECIMAL_PATTERN);


    private final AccountRepository accountRepository;
    private final CommissionRepository commissionRepository;
    private final SecurityKeyRepository securityKeyRepository;
    private final CommissionDTOMapper commissionDTOMapper;

    public ApplicationServiceImpl(AccountRepository accountRepository, CommissionRepository commissionRepository, SecurityKeyRepository securityKeyRepository, CommissionDTOMapper commissionDTOMapper) {
        this.accountRepository = accountRepository;
        this.commissionRepository = commissionRepository;
        this.securityKeyRepository = securityKeyRepository;
        this.commissionDTOMapper = commissionDTOMapper;
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

            String rateString = getRateForCurrencyPair(currencies[0], currencies[1], currenciesList);
            double rateDouble = Double.parseDouble(rateString);
            BigDecimal rate = BigDecimal.valueOf(rateDouble);

            return new RateDTO(currencies[0], currencies[1], rate, LocalDate.parse(date));

        } catch (IOException | InterruptedException | OfficialRateFetchException e) {
            throw new OfficialRateFetchException("Failed to fetch official exchange rate.");
        }
    }

    @Override
    public ConversionDTO performConversion(ConversionDTO conversionDTO) {
        Optional<Account> fromAccountOptional = accountRepository.getAccountByCurrencyName(conversionDTO.from());
        Optional<Account> toAccountOptional = accountRepository.getAccountByCurrencyName(conversionDTO.to());

        if (fromAccountOptional.isEmpty())
            throw new NotFoundException("From account does not exists!");

        if (toAccountOptional.isEmpty())
            throw new NotFoundException("To account does not exists!");

        Account fromAccount = fromAccountOptional.get();
        Account toAccount = toAccountOptional.get();


        Commission commission = getCommission(conversionDTO.from(), conversionDTO.to());

        double moneyToTransfer = conversionDTO.amount() * (100 - commission.getCommissionAmount()) / 100;
        log.info(String.valueOf(moneyToTransfer));

        if (fromAccount.getAmount() >= moneyToTransfer) {

            fromAccount.setAmount(fromAccount.getAmount() - moneyToTransfer);
            accountRepository.save(fromAccount);

            double amountToAddToAccount = toAccount.getAmount() + (conversionDTO.amount() * commission.getConversionRate());
            toAccount.setAmount(amountToAddToAccount);
            accountRepository.save(toAccount);

            return new ConversionDTO(conversionDTO.from(), conversionDTO.to(), amountToAddToAccount);
        } else {
            // Throw an exception or handle the insufficient funds scenario as needed
            // throw new InsufficientFundsException("Insufficient funds in the account for conversion.");
            log.warn("NOt found");
            return null;
        }
    }

    private Commission getCommission(String from, String to) {
        return commissionRepository.getCommissionByFromCurrencyAndToCurrency(from, to).orElseThrow(() -> new NotFoundException("Commission not found for the specified currencies"));
    }

    @Override
    public CommissionDTO setCommission(String secretKey, CommissionDTO commissionDTO) {
        if (!secretKey.equals(securityKeyRepository.getSecretKey()))
            throw new SecurityException("Security key is not equal!");

        Optional<Commission> commissionOptional = commissionRepository.getCommissionByFromCurrencyAndToCurrency(commissionDTO.from(), commissionDTO.to());
        if (commissionOptional.isEmpty())
            throw new NotFoundException("This pair does not exist!");

        Commission commission = commissionOptional.get();
        commission.setCommissionAmount(commissionDTO.commissionAmount());

        return commissionDTOMapper.apply(commissionRepository.save(commission));
    }

    private String buildApiUrl(String currency, String date) {
        return API_BASE_URL + currency + "/" + date + "/";
    }

    private String getRateForCurrencyPair(String from, String to, List<CurrencyDTO> currenciesList) {

        if (from.equals("UZS"))
            return DECIMAL_FORMATTER.format(BigDecimal.valueOf(1.0 / Double.parseDouble(currenciesList.get(0).Rate())));
        else if (to.equals("UZS"))
            return DECIMAL_FORMATTER.format(BigDecimal.valueOf(Double.parseDouble(currenciesList.get(0).Rate())));
        else
            return DECIMAL_FORMATTER.format(BigDecimal.valueOf(Double.parseDouble(currenciesList.get(0).Rate())));
    }

    private Double calculate(Commission commission, Double amount) {
        return (((100 - commission.getCommissionAmount()) * amount) / 100) * commission.getConversionRate();
    }
}
