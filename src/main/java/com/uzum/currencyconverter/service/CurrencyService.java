package com.uzum.currencyconverter.service;


import com.uzum.currencyconverter.dto.CurrencyDTO;

import java.util.List;

public interface CurrencyService {
    void updateCurrency();

    List<CurrencyDTO> fetchCurrencies();
}
