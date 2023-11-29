//package com.uzum.currencyconverter.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@EqualsAndHashCode
//@Table(name = "conversion_log")
//public class ConversionLog {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "from_currency", nullable = false)
//    private String fromCurrency;
//
//    @Column(name = "to_currency", nullable = false)
//    private String toCurrency;
//
//    @Column(name = "amount", nullable = false)
//    private Double amount;
//
//    @Column(name = "converted_amount", nullable = false)
//    private Double convertedAmount;
//
//    @Column(name = "commission_amount", nullable = false)
//    private Double commissionAmount;
//
//    @Column(name = "conversion_date", nullable = false)
//    private LocalDateTime conversionDate;
//}
