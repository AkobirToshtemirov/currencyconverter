package com.uzum.currencyconverter.dto;

public record CurrencyDTO(
        int id,
        String Code,
        String Ccy,
        String CcyNm_RU,
        String CcyNm_UZ,
        String CcyNm_UZC,
        String CcyNm_EN,
        String Nominal,
        String Rate,
        String Diff,
        String Date
) {
}
