package com.example.IPS.IPS.service;

import com.example.IPS.IPS.dto.*;
import com.example.IPS.IPS.entity.AlertLog;
import com.example.IPS.IPS.entity.Transactions;
import com.example.IPS.IPS.repository.AlertLogRepo;
import com.example.IPS.IPS.repository.TransactionsRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class TransactionService {
    private final String status = "FAILURE";

    private final TransactionsRepo transactionsRepo;
    private final AlertingServices alertingService;
    private final AlertLogRepo alertLogRepo;
    @Value("${app.alert.threshold.percentage}")
    private double failureThresholdPercentage;
    @Value("${app.alert.threshold.amount}")
    private double failureThresholdAmount = 1000;

    @Autowired
    public TransactionService(
            TransactionsRepo transactionsRepo,
            AlertingServices alertingService,
            AlertLogRepo alertLogRepo) {
        this.transactionsRepo = transactionsRepo;
        this.alertingService = alertingService;
        this.alertLogRepo = alertLogRepo;

    }

    public Page<Transactions> getAllTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionsRepo.findAll(pageable);
    }


    public List<Transactions> getAllTransactionsFailures(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return transactionsRepo.findByStatusAndTimestampBetween(status, startOfDay, endOfDay);
    }

    public List<Transactions> getAllTransactionsFailuresByType(String type, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return transactionsRepo.findByTypeAndStatusAndTimestampBetween(type, status, startOfDay, endOfDay);
    }

    //getAllTransactionsFailuresSummaryByType
    public TransactionsSummaryDTO getAllTransactionsFailuresSummaryByType(String type, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // Fetch all transactions for that type/date
        List<Transactions> allTransactions = transactionsRepo.findAllByTypeAndTimestampBetween(type, startOfDay, endOfDay);

        long totalTransactions = allTransactions.size();

        // Filter failed transactions
        List<Transactions> failedTransactionsList = allTransactions.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("FAILURE"))
                .toList();

        long failedTransactions = failedTransactionsList.size();
        double failurePercentage = totalTransactions > 0 ? (failedTransactions * 100.0 / totalTransactions) : 0;

        double totalFailedAmount = failedTransactionsList.stream()
                .mapToDouble(Transactions::getAmount)
                .sum();

        return new TransactionsSummaryDTO(type, date, totalTransactions, failedTransactions, failurePercentage, totalFailedAmount);
    }


    public Transactions getTransactionById(String id) {
        Transactions transaction = transactionsRepo.findByTransactionId(id);

        return transaction;
    }

    public List<Transactions> getTransactionsByDateRange(String type, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return transactionsRepo.findAllByStatusAndTypeAndTimestampBetween(status,type, start, end);
    }

//    public List<DailySummary> getDailySummaryByDateRange(LocalDate startDate, LocalDate endDate) {
//        LocalDateTime start = startDate.atStartOfDay();
//        LocalDateTime end = endDate.atTime(LocalTime.MAX);
//
//        List<Transactions> transactions = transactionsRepo.findByTimestampBetween(start, end);
//
//        // Group transactions by date (sorted)
//        Map<LocalDate, List<Transactions>> byDate = transactions.stream()
//                .collect(Collectors.groupingBy(
//                        t -> t.getTimestamp().toLocalDate(),
//                        TreeMap::new, // keeps dates sorted
//                        Collectors.toList()
//                ));
//
//        List<DailySummary> summaries = new ArrayList<>();
//        for (Map.Entry<LocalDate, List<Transactions>> entry : byDate.entrySet()) {
//            LocalDate date = entry.getKey();
//            List<Transactions> daily = entry.getValue();
//
//            // Filter failures once
//            List<Transactions> failures = daily.stream()
//                    .filter(t -> "FAILURE".equalsIgnoreCase(t.getStatus()))
//                    .toList();
//
//            long totalFailures = failures.size();
//            double totalAmount = failures.stream()
//                    .mapToDouble(Transactions::getAmount)
//                    .sum();
//
//            Map<String, Long> failureByType = failures.stream()
//                    .collect(Collectors.groupingBy(Transactions::getType, Collectors.counting()));
//
//            summaries.add(new DailySummary(date, totalFailures, totalAmount, failureByType));
//        }
//
//        return summaries; // already sorted by TreeMap
//    }

    public SummaryResponse getDailyAndAggregateSummary(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Transactions> transactions = transactionsRepo.findByTimestampBetween(start, end);

        // Group transactions by date
        Map<LocalDate, List<Transactions>> byDate = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTimestamp().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<DailySummary> dailySummaries = new ArrayList<>();

        long totalFailures = 0;
        double totalAmount = 0;
        Map<String, Long> aggregateFailureByType = new HashMap<>();

        for (Map.Entry<LocalDate, List<Transactions>> entry : byDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<Transactions> daily = entry.getValue();

            // Filter failures once
            List<Transactions> failures = daily.stream()
                    .filter(t -> "FAILURE".equalsIgnoreCase(t.getStatus()))
                    .toList();

            long dailyFailures = failures.size();
            double dailyAmount = failures.stream().mapToDouble(Transactions::getAmount).sum();

            Map<String, Long> dailyFailureByType = failures.stream()
                    .collect(Collectors.groupingBy(Transactions::getType, Collectors.counting()));

            // Add to daily summaries
            dailySummaries.add(new DailySummary(date, dailyFailures, dailyAmount, dailyFailureByType));

            // Aggregate totals
            totalFailures += dailyFailures;
            totalAmount += dailyAmount;
            dailyFailureByType.forEach((type, count) ->
                    aggregateFailureByType.merge(type, count, Long::sum));
        }

        return new SummaryResponse(dailySummaries, totalFailures, totalAmount, aggregateFailureByType);
    }


    public DailyTypeSummary getDailySummary(LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // fetch all transactions of that type for the day
        List<Transactions> transactions = transactionsRepo.findByTimestampBetween(startOfDay, endOfDay);

        long totalFailures = transactions.stream()
                .filter(t -> "FAILURE".equalsIgnoreCase(t.getStatus()))
                .count();

        long totalSuccesses = transactions.stream()
                .filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus()))
                .count();

        double failedAmount = transactions.stream()
                .filter(t -> "FAILED".equalsIgnoreCase(t.getStatus()))
                .mapToDouble(t -> t.getAmount())
                .sum();

        double successAmount = transactions.stream()
                .filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus()))
                .mapToDouble(t -> t.getAmount())
                .sum();

        long totalTransactions = totalFailures + totalSuccesses;

        double failurePercentage = totalTransactions == 0 ? 0 : (totalFailures * 100.0 / totalTransactions);
        double successPercentage = totalTransactions == 0 ? 0 : (totalSuccesses * 100.0 / totalTransactions);

        return new DailyTypeSummary(
                date,
                totalFailures,
                totalSuccesses,
                failedAmount,
                successAmount,
                failurePercentage,
                successPercentage
        );
    }


    public List<Transactions> getDailyFailedTransactions(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // fetch all transactions of that type for the day
        return transactionsRepo.findByTimestampBetween(startOfDay, endOfDay);
    }

    public List<Transactions> getDailyFailedTransactionsByType(String type, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // fetch all transactions of that type for the day
//        return transactionsRepo.findAllByTypeAndTimestampBetween(type, startOfDay, endOfDay);
        return transactionsRepo.findAllByStatusAndTypeAndTimestampBetween(status, type, startOfDay, endOfDay);
    }


    public DailyTypeSummary getDailySummaryByType(LocalDate date, String type) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // fetch all transactions of that type for the day
        List<Transactions> transactions = transactionsRepo.findAllByTypeAndTimestampBetween(type, startOfDay, endOfDay);

        long totalFailures = transactions.stream()
                .filter(t -> "FAILURE".equalsIgnoreCase(t.getStatus()))
                .count();

        long totalSuccesses = transactions.stream()
                .filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus()))
                .count();

        double failedAmount = transactions.stream()
                .filter(t -> "FAILED".equalsIgnoreCase(t.getStatus()))
                .mapToDouble(t -> t.getAmount())
                .sum();

        double successAmount = transactions.stream()
                .filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus()))
                .mapToDouble(t -> t.getAmount())
                .sum();

        long totalTransactions = totalFailures + totalSuccesses;

        double failurePercentage = totalTransactions == 0 ? 0 : (totalFailures * 100.0 / totalTransactions);
        double successPercentage = totalTransactions == 0 ? 0 : (totalSuccesses * 100.0 / totalTransactions);

        return new DailyTypeSummary(
                date,
                totalFailures,
                totalSuccesses,
                failedAmount,
                successAmount,
                failurePercentage,
                successPercentage
        );
    }

    public TransactionStatsDTO getStatsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // fetch all transactions of that type for the day
        List<Transactions> transactions = transactionsRepo.findByTimestampBetween(startOfDay, endOfDay);


        long totalSuccesses = transactions.stream().filter(Transactions::isSuccess).count();
        long totalFailures = transactions.stream().filter(Transactions::isFailure).count();

        double successAmount = transactions.stream()
                .filter(Transactions::isSuccess)
                .mapToDouble(t -> t.getAmount())
                .sum();

        double failedAmount = transactions.stream()
                .filter(Transactions::isFailure)
                .mapToDouble(t -> t.getAmount())
                .sum();

        double failurePercentage = transactions.isEmpty() ? 0 : (totalFailures * 100.0 / transactions.size());
        double successPercentage = transactions.isEmpty() ? 0 : (totalSuccesses * 100.0 / transactions.size());

        if (failurePercentage > failureThresholdPercentage || failedAmount > failureThresholdAmount) {
            alertingService.sendAlert(date, totalFailures, failedAmount, failurePercentage);
        }

        return new TransactionStatsDTO(
                date,
                totalFailures,
                totalSuccesses,
                failedAmount,
                successAmount,
                failurePercentage,
                successPercentage
        );
    }

    public TransactionStatsDTO getStatsByDateByType(String type, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);


        // fetch all transactions of that type for the day
        List<Transactions> transactions = transactionsRepo.findAllByTypeAndTimestampBetween(type, startOfDay, endOfDay);


        long totalSuccesses = transactions.stream().filter(Transactions::isSuccess).count();
        long totalFailures = transactions.stream().filter(Transactions::isFailure).count();

        double successAmount = transactions.stream()
                .filter(Transactions::isSuccess)
                .mapToDouble(t -> t.getAmount())
                .sum();

        double failedAmount = transactions.stream()
                .filter(Transactions::isFailure)
                .mapToDouble(t -> t.getAmount())
                .sum();

        double failurePercentage = transactions.isEmpty() ? 0 : (totalFailures * 100.0 / transactions.size());
        double successPercentage = transactions.isEmpty() ? 0 : (totalSuccesses * 100.0 / transactions.size());

        if (failurePercentage > failureThresholdPercentage || failedAmount > failureThresholdAmount) {
            alertingService.sendAlert(date, totalFailures, failedAmount, failurePercentage);
        }

        return new TransactionStatsDTO(
                date,
                totalFailures,
                totalSuccesses,
                failedAmount,
                successAmount,
                failurePercentage,
                successPercentage
        );
    }


// save transaction
//    public TransactionDTO saveTransaction(TransactionDTO dto) {
//        Transactions transaction = new Transactions();
//        transaction.setTransactionId(dto.getTransactionId());
//        transaction.setAmount(dto.getAmount());
//        transaction.setType(dto.getType());
//        transaction.setTimestamp(dto.getTimestamp());
//        transaction.setStatus(dto.getStatus());
//        transaction.setReason(dto.getReason());
//
//        Transactions saved = transactionsRepo.save(transaction);
//
//        dto.setId(saved.getId());
//        return dto;
//    }


    public TransactionDTO saveTransaction(TransactionDTO dto) {
        Transactions transaction = new Transactions();
        transaction.setTransactionId(dto.getTransactionId());
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setTimestamp(dto.getTimestamp());
        transaction.setStatus(dto.getStatus());
        transaction.setReason(dto.getReason());

        // Save transaction
        Transactions saved = transactionsRepo.save(transaction);

        //  Immediately check thresholds for this day
        checkThresholdAndSendAlert(saved.getTimestamp().toLocalDate());

        //  Return saved DTO
        dto.setId(saved.getId());
        return dto;
    }

    // check immediately always
//    private void checkThresholdAndSendAlert(LocalDate date) {
//        LocalDateTime startOfDay = date.atStartOfDay();
//        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
//        List<Transactions> transactions = transactionsRepo.findByTimestampBetween(startOfDay, endOfDay);
//
//        long totalFailures = transactions.stream()
//                .filter(t -> "FAILURE".equalsIgnoreCase(t.getStatus()))
//                .count();
//        long totalSuccesses = transactions.size() - totalFailures;
//
//        double failedAmount = transactions.stream()
//                .filter(t -> "FAILURE".equalsIgnoreCase(t.getStatus()))
//                .mapToDouble(Transactions::getAmount)
//                .sum();
//
//        double successAmount = transactions.stream()
//                .filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus()))
//                .mapToDouble(Transactions::getAmount)
//                .sum();
//
//        double failurePercentage = transactions.isEmpty() ? 0
//                : (totalFailures * 100.0 / transactions.size());
//
//        // Trigger alert immediately if thresholds exceeded
//        if (failurePercentage > failureThresholdPercentage || failedAmount > failureThresholdAmount) {
//            alertingService.sendAlert(date, totalFailures, failedAmount, failurePercentage);
//        }
//    }


    private void checkThresholdAndSendAlert(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Transactions> transactions = transactionsRepo.findByTimestampBetween(startOfDay, endOfDay);

        long totalFailures = transactions.stream()
                .filter(t -> "FAILURE".equalsIgnoreCase(t.getStatus()))
                .count();

        double failedAmount = transactions.stream()
                .filter(t -> "FAILURE".equalsIgnoreCase(t.getStatus()))
                .mapToDouble(Transactions::getAmount)
                .sum();

        double failurePercentage = transactions.isEmpty() ? 0
                : (totalFailures * 100.0 / transactions.size());

        AlertLog alertLog = alertLogRepo.findByDate(date).orElseGet(() -> {
            AlertLog newLog = new AlertLog();
            newLog.setDate(date);
            return newLog;
        });

        boolean shouldSendAlert = false;
        StringBuilder alertMessage = new StringBuilder();

        // FAILURE PERCENTAGE LOGIC
        if (failurePercentage > failureThresholdPercentage) {
            // Threshold exceeded
            alertLog.setFailureThresholdExceeded(true);

            if (!alertLog.isFailureAlertSent() || lastTransactionIsFailure(transactions)) {
                alertMessage.append("Failure Percentage exceeded: ").append(failurePercentage).append("%\n");
                alertLog.setFailureAlertSent(true);
                shouldSendAlert = true;
            }
        } else {
            // Threshold back to normal
            alertLog.setFailureThresholdExceeded(false);
            alertLog.setFailureAlertSent(false);
        }

        // FAILED AMOUNT LOGIC
        if (failedAmount > failureThresholdAmount) {
            alertLog.setAmountThresholdExceeded(true);

            if (!alertLog.isAmountAlertSent() || lastTransactionIsFailure(transactions)) {
                alertMessage.append("Failed Amount exceeded: $").append(failedAmount).append("\n");
                alertLog.setAmountAlertSent(true);
                shouldSendAlert = true;
            }
        } else {
            alertLog.setAmountThresholdExceeded(false);
            alertLog.setAmountAlertSent(false);
        }

        if (shouldSendAlert) {
            alertingService.sendAlert(date, totalFailures, failedAmount, failurePercentage);
        }

        alertLogRepo.save(alertLog);
    }

    // Helper to check if last transaction is failure
    private boolean lastTransactionIsFailure(List<Transactions> transactions) {
        if (transactions.isEmpty()) return false;
        return "FAILURE".equalsIgnoreCase(transactions.get(transactions.size() - 1).getStatus());
    }


}
