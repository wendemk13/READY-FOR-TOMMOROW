package com.example.IPS.IPS.service;

import com.example.IPS.IPS.repository.TransactionsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScheduledTransactionAlertService {

    private final TransactionsRepo transactionRepository;
    private final AlertingServices alertService;
    private final TransactionService transactionStatsService;

    @Scheduled(cron = "0  0 * * * ?")
    public void checkDailyFailedTransactions() {

        System.out.println("Scheduled emailing sent at " + LocalDateTime.now());

        transactionStatsService.getStatsByDate(LocalDate.now());
    }

}
