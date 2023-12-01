package com.uzum.currencyconverter.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uzum.currencyconverter.dto.ConversionDTO;
import com.uzum.currencyconverter.dto.CurrencyDTO;
import com.uzum.currencyconverter.dto.RateDTO;
import com.uzum.currencyconverter.entity.Account;
import com.uzum.currencyconverter.entity.Commission;
import com.uzum.currencyconverter.exception.NotEnoughMoneyException;
import com.uzum.currencyconverter.exception.NotFoundException;
import com.uzum.currencyconverter.exception.OfficialRateFetchException;
import com.uzum.currencyconverter.repository.AccountRepository;
import com.uzum.currencyconverter.repository.CommissionRepository;
import com.uzum.currencyconverter.service.api.ApplicationService;
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

@Service
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {
    private static final String API_BASE_URL = "https://cbu.uz/ru/arkhiv-kursov-valyut/json/";
    private static final String DECIMAL_PATTERN = "#.######";
    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat(DECIMAL_PATTERN);
    private static final String UZS = "UZS";


    private final AccountRepository accountRepository;
    private final CommissionRepository commissionRepository;

    public ApplicationServiceImpl(AccountRepository accountRepository, CommissionRepository commissionRepository) {
        this.accountRepository = accountRepository;
        this.commissionRepository = commissionRepository;
    }

    @Override
    public ConversionDTO getConversion(String from, String to, Double amount) {
        Double result;
        if (from.equals(UZS) || to.equals(UZS)) {
            result = calculateCommission(getCommission(from, to), amount);
        } else {
            result = calculateCommission(getCommission(from, UZS), amount);
            result = calculateCommission(getCommission(UZS, to), result);
        }

        return new ConversionDTO(from, to, DECIMAL_FORMATTER.format(result));
    }

    @Override
    public RateDTO getOfficialRate(String date, String pair) {
        String[] currencies = pair.split("/");

        if (!UZS.equals(currencies[0]) && !UZS.equals(currencies[1])) {
            Commission commissionToUZS = getCommission(currencies[0], UZS);
            Commission commissionFromUZS = getCommission(UZS, currencies[1]);

            Double rateToUZS = calculateCommission(commissionToUZS, 1.0);
            Double finalRate = calculateCommission(commissionFromUZS, rateToUZS);

            return createRateDTO(currencies[0], currencies[1], date, finalRate);
        }

        try {
            String apiUrl = buildApiUrl(UZS.equals(currencies[0]) ? currencies[1] : currencies[0], date);
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new OfficialRateFetchException("Failed to fetch official exchange rate");
            }

            String rate = getRateFromResponse(response, currencies);

            return createRateDTO(currencies[0], currencies[1], date, Double.parseDouble(rate));

        } catch (IOException | InterruptedException | OfficialRateFetchException e) {
            Thread.currentThread().interrupt();
            throw new OfficialRateFetchException("Failed to fetch official exchange rate");
        }
    }

    @Override
    public ConversionDTO performConversion(ConversionDTO dto) {
        Account fromAccount = getAccountOrThrow(dto.from());
        Account toAccount = getAccountOrThrow(dto.to());

        if (UZS.equals(dto.from()) || UZS.equals(dto.to())) {

            Commission commission = getCommission(dto.from(), dto.to());
            BigDecimal moneyToTransfer = getMoneyToTransfer(dto.amount());

            validateSufficientFunds(fromAccount, moneyToTransfer);

            BigDecimal moneyToSendToReceiverAccount = calculateAmountOfMoneyToSendToReceiverAccount(moneyToTransfer, commission);

            updateAccountBalances(fromAccount, toAccount, moneyToTransfer, moneyToSendToReceiverAccount);

            String convertedAmount = DECIMAL_FORMATTER.format(moneyToSendToReceiverAccount);
            return new ConversionDTO(dto.from(), dto.to(), convertedAmount);

        } else {
            Commission commission1 = getCommission(dto.from(), UZS);
            Commission commission2 = getCommission(UZS, dto.to());

            BigDecimal moneyToTransfer = getMoneyToTransfer(dto.amount());

            validateSufficientFunds(fromAccount, moneyToTransfer);

            BigDecimal moneyToSendToUzsAccount = calculateAmountOfMoneyToSendToReceiverAccount(moneyToTransfer, commission1);
            BigDecimal moneyToSendToReceiverAccount = calculateAmountOfMoneyToSendToReceiverAccount(moneyToSendToUzsAccount, commission2);

            updateAccountBalances(fromAccount, toAccount, moneyToTransfer, moneyToSendToReceiverAccount);


            String convertedAmount = DECIMAL_FORMATTER.format(moneyToSendToReceiverAccount);
            return new ConversionDTO(dto.from(), dto.to(), convertedAmount);

        }

    }

    private void updateAccountBalances(Account fromAccount, Account toAccount, BigDecimal moneyToTransfer, BigDecimal moneyToSendToReceiverAccount) {
        fromAccount.setAmount(fromAccount.getAmount().subtract(moneyToTransfer));
        toAccount.setAmount(toAccount.getAmount().add(moneyToSendToReceiverAccount));

        accountRepository.saveAll(List.of(fromAccount, toAccount));
    }

    private void validateSufficientFunds(Account fromAccount, BigDecimal moneyToTransfer) {
        if (fromAccount.getAmount().doubleValue() < moneyToTransfer.doubleValue()) {
            throw new NotEnoughMoneyException("Not enough money in account. ");
        }
    }

    private BigDecimal getMoneyToTransfer(String amount) {
        return BigDecimal.valueOf(Double.parseDouble(amount));
    }

    private Account getAccountOrThrow(String currency) {
        return accountRepository.findByCurrencyName(currency)
                .orElseThrow(() -> new NotFoundException("Account not found!"));
    }

    private String getRateFromResponse(HttpResponse<String> response, String[] currencies) {
        Gson gson = new Gson();
        Type currencyListType = new TypeToken<List<CurrencyDTO>>() {
        }.getType();
        List<CurrencyDTO> currenciesList = gson.fromJson(response.body(), currencyListType);

        return calculateRateForCurrencyPair(currencies[0], currenciesList);
    }

    private static BigDecimal calculateAmountOfMoneyToSendToReceiverAccount(BigDecimal moneyToTransfer, Commission commission) {
        return BigDecimal.valueOf(moneyToTransfer.doubleValue() * commission.getConversionRate().doubleValue() * ((100 - commission.getCommissionAmount()) * 0.01));
    }

    private Double calculateCommission(Commission commission, Double amount) {
        return (((100 - commission.getCommissionAmount()) * amount) / 100) * commission.getConversionRate().doubleValue();
    }

    private String buildApiUrl(String currency, String date) {
        return API_BASE_URL + currency + "/" + date + "/";
    }

    private String calculateRateForCurrencyPair(String fromCurrency, List<CurrencyDTO> currenciesList) {
        if (UZS.equals(fromCurrency)) {
            return DECIMAL_FORMATTER.format(BigDecimal.valueOf(1.0 / Double.parseDouble(currenciesList.get(0).Rate())));
        } else {
            return DECIMAL_FORMATTER.format(BigDecimal.valueOf(Double.parseDouble(currenciesList.get(0).Rate())));
        }
    }

    private RateDTO createRateDTO(String from, String to, String date, Double rate) {
        return new RateDTO(from, to, DECIMAL_FORMATTER.format(BigDecimal.valueOf(rate)), LocalDate.parse(date));
    }

    public Commission getCommission(String from, String to) {
        return commissionRepository.findByFromCurrencyAndToCurrency(from, to).
                orElseThrow(() -> new NotFoundException("Commission not found for the specified currencies"));
    }
}
