package com.uzum.currencyconverter.service;

import com.uzum.currencyconverter.dto.CommissionDTO;
import com.uzum.currencyconverter.dto.ConversionDTO;
import com.uzum.currencyconverter.dto.RateDTO;

public interface ApplicationService {
    ConversionDTO getConversion(String from, String to, Double amount);

    RateDTO getOfficialRate(String date, String pair);

    ConversionDTO performConversion(ConversionDTO conversionDTO);

    CommissionDTO setCommission(String secretKey, CommissionDTO commissionDTO);
}
