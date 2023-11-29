package com.uzum.currencyconverter.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RateDTO(
        String fromCurrency,
        String toCurrency,
        BigDecimal rate,
        LocalDate date
) {
}
