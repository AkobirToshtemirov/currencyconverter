package com.uzum.currencyconverter.service.api;

import com.uzum.currencyconverter.dto.CommissionDTO;
import com.uzum.currencyconverter.dto.CurrencyDTO;

import java.util.List;

public interface CurrencyService {
    void updateCurrency();

    List<CurrencyDTO> fetchCurrencies();

    CommissionDTO setCommission(String secretKey, CommissionDTO dto);
}
