package com.example.IPS.IPS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsSummaryDTO {
    private String type;
    private LocalDate date;
    private long totalTransactions;
    private long failedTransactions;
    private double failurePercentage;
    private double totalFailedAmount;
}
