package com.uzum.currencyconverter.dto;

import java.time.LocalDate;

public record RateDTO(
        String fromCurrency,
        String toCurrency,
        String rate,
        LocalDate date
) {
}
