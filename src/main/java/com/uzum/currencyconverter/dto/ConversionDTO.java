package com.uzum.currencyconverter.dto;

public record ConversionDTO(
        String from,
        String to,
        String amount
) {
}
