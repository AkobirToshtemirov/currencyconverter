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
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "currency_name", nullable = false)
    private String currencyName;

    @Column(nullable = false, precision = 38, scale = 6)
    private BigDecimal amount;
}
