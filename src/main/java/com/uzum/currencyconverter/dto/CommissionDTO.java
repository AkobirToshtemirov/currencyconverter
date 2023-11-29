package com.uzum.currencyconverter.dto;

public record CommissionDTO(
        String from,
        String to,
        Float commissionAmount
) {
}
