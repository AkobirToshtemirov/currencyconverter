package com.uzum.currencyconverter.controller;

import com.uzum.currencyconverter.dto.CommissionDTO;
import com.uzum.currencyconverter.dto.ConversionDTO;
import com.uzum.currencyconverter.dto.RateDTO;
import com.uzum.currencyconverter.service.ApplicationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/app")
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/convert")
    public ConversionDTO getConversion(@RequestParam String from, @RequestParam String to, @RequestParam Double amount) {
        return applicationService.getConversion(from, to, amount);
    }

    @PostMapping("/convert")
    public ConversionDTO performConversion(@RequestBody ConversionDTO conversionDTO) {
        return applicationService.performConversion(conversionDTO);
    }

    @GetMapping("/officialrates")
    public RateDTO getOfficialRate(@RequestParam String date, @RequestParam String pair) {
        return applicationService.getOfficialRate(date, pair);
    }

    @PostMapping("/setcomission")
    public CommissionDTO setCommission(@RequestHeader("secret-key") String secretKey, @RequestBody CommissionDTO commissionDTO) {
        return applicationService.setCommission(secretKey, commissionDTO);
    }
}
