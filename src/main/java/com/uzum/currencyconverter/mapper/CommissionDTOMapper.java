package com.uzum.currencyconverter.mapper;

import com.uzum.currencyconverter.dto.CommissionDTO;
import com.uzum.currencyconverter.entity.Commission;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class CommissionDTOMapper implements Function<Commission, CommissionDTO> {
    @Override
    public CommissionDTO apply(Commission commission) {
        return new CommissionDTO(
                commission.getFromCurrency(),
                commission.getToCurrency(),
                commission.getCommissionAmount());
    }
}
