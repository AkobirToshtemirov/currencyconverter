package com.uzum.currencyconverter.controller;

import com.uzum.currencyconverter.dto.CommissionDTO;
import com.uzum.currencyconverter.dto.ConversionDTO;
import com.uzum.currencyconverter.dto.RateDTO;
import com.uzum.currencyconverter.service.api.ApplicationService;
import com.uzum.currencyconverter.service.api.CurrencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/app")
public class ApplicationController {
    private final ApplicationService applicationService;
    private final CurrencyService currencyService;


    public ApplicationController(ApplicationService applicationService, CurrencyService currencyService) {
        this.applicationService = applicationService;
        this.currencyService = currencyService;
    }

    @GetMapping("/convert")
    public ResponseEntity<ConversionDTO> getConversion(@RequestParam String from, @RequestParam String to, @RequestParam Double amount) {
        return ResponseEntity.ok(applicationService.getConversion(from, to, amount));
    }

    @PostMapping("/convert")
    public ResponseEntity<ConversionDTO> performConversion(@RequestBody ConversionDTO conversionDTO) {
        return ResponseEntity.ok(applicationService.performConversion(conversionDTO));
    }

    @GetMapping("/officialrates")
    public ResponseEntity<RateDTO> getOfficialRate(@RequestParam String date, @RequestParam String pair) {
        return ResponseEntity.ok(applicationService.getOfficialRate(date, pair));
    }

    @PostMapping("/setcomission")
    public ResponseEntity<CommissionDTO> setCommission(@RequestHeader("secret-key") String secretKey, @RequestBody CommissionDTO commissionDTO) {
        return ResponseEntity.ok(currencyService.setCommission(secretKey, commissionDTO));
    }
}
