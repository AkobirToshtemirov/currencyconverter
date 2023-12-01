package com.uzum.currencyconverter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "commission")
public class Commission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency;

    @Column(name = "commission_amount", nullable = false)
    private Float commissionAmount;

    @Column(name = "conversion_rate", nullable = false, precision = 38, scale = 20)
    private BigDecimal conversionRate;

    public Commission(String fromCurrency, String toCurrency, Float commissionAmount, BigDecimal conversionRate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.commissionAmount = commissionAmount;
        this.conversionRate = conversionRate;
    }
}
