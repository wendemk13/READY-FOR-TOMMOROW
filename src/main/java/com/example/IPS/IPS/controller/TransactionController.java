package com.example.IPS.IPS.controller;

import com.example.IPS.IPS.dto.*;
import com.example.IPS.IPS.entity.Transactions;
import com.example.IPS.IPS.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/monitor")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    //    get all failures
    @GetMapping("/allfailures")
    public ResponseEntity<List<Transactions>> getALlTransactionfailures(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        {
            if (date == null) {
                date = LocalDate.now();
            }
            return ResponseEntity.ok(transactionService.getAllTransactionsFailures(date));
        }

    }

    //    get all failures type
    @GetMapping(value={"/failures","/failures/{type}"})
    public ResponseEntity<List<Transactions>> getALlTransactionfailuresByType(
            @PathVariable(value ="type",required = false) String type,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        {
            if (type==null){
                type="IPS";
            }
            if (date == null) {
                date = LocalDate.now();
            }
            return ResponseEntity.ok(transactionService.getAllTransactionsFailuresByType(type, date));
        }

    }


    //    get all failures summary by type and date
    @GetMapping(value = {"/failures/summary", "/failures/summary/{type}"})
    public ResponseEntity<TransactionsSummaryDTO> getTransactionsFailuresSummaryByType(
            @PathVariable(value = "type") String type,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (type == null || type.isEmpty()) {
            type = "IPS";
        }
        if (date == null) {
            date = LocalDate.now();
        }

        TransactionsSummaryDTO summary = transactionService.getAllTransactionsFailuresSummaryByType(type, date);
        return ResponseEntity.ok(summary);
    }


    //    get summary by date
//    @GetMapping("/daily/summary")
//    public ResponseEntity<DailyTypeSummary> getDailySummary(
//            @RequestParam(value = "date", required = false)
//            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//        if (date == null) {
//            date = LocalDate.now();
//        }
//        return ResponseEntity.ok(transactionService.getDailySummary(date));
//    }

    //get daily summary by type and date
    @GetMapping(value={"/daily/summary/{type}","/daily/summary"})
    public ResponseEntity<DailyTypeSummary> getDailySummaryByType(
            @PathVariable String type,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return ResponseEntity.ok(transactionService.getDailySummaryByType(date, type));
    }

//    //    get list of failed transactions by date
//    @GetMapping("/daily/failed-transactions")
//    public ResponseEntity<List<Transactions>> getDailyFailedTransactions(
//            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//        if (date == null) {
//            date = LocalDate.now();
//        }
//
//
//        return ResponseEntity.ok(transactionService.getDailyFailedTransactions(date));
//    }

    //    get list of failed transactions by type and status
    @GetMapping(value = {"/daily/failed-transactions", "/daily/failed-transactions/{type}"})
    public ResponseEntity<List<Transactions>> getDailyFailedTransactionsByType(
            @PathVariable(value = "type") String type,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        if (type == null) {
            type = "IPS";
        }

        return ResponseEntity.ok(transactionService.getDailyFailedTransactionsByType(type, date));
    }


    // transaction save
    @PostMapping("/saveTransaction")
    public ResponseEntity<TransactionDTO> saveTransaction(@RequestBody TransactionDTO dto) {
        TransactionDTO saved = transactionService.saveTransaction(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Transactions> getTransactionById(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }


    //    get transactions by date range
    @GetMapping(value={"/range","/range/{type}"})
    public ResponseEntity<List<Transactions>> getAllTransactionsByDateRange(
            @PathVariable(value="type",required = false) String type,
            @RequestParam(value = "startDate",required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate",required = false)  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (type == null) {
            type = "IPS";
        }
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        List<Transactions> transactions = transactionService.getTransactionsByDateRange(type, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    //    get transactions summary by date range
//    @GetMapping("/range/summary")
//    public ResponseEntity<List<DailySummary>> getDailySummary(
//            @RequestParam(value = "startDate",required = false)@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam(value = "endDate",required = false)  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
//
//        if (startDate == null) {
//            startDate = LocalDate.now();
//        }
//        if (endDate == null) {
//            endDate = LocalDate.now();
//        }
//        List<DailySummary> summaries = transactionService.getDailySummaryByDateRange(startDate, endDate);
//        return ResponseEntity.ok(summaries);
//    }
    @GetMapping("/range/summary")
    public ResponseEntity<SummaryResponse> getDailyAndAggregateSummary(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.now();

        SummaryResponse summary = transactionService.getDailyAndAggregateSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }


    //======================================
//    get general summary
//    get range by type

    //get all transaction failed and success
//    @GetMapping("/all")
//    public ResponseEntity<Page<Transactions>> findAllTransactions(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size
//    ) {
//        return ResponseEntity.ok(
//                transactionService.getAllTransactions(page, size)
//        );
//    }
    @GetMapping("/all")
    public ResponseEntity<PageResponse<Transactions>> findAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<Transactions> transactions =
                transactionService.getAllTransactions(page, size);

        return ResponseEntity.ok(PageResponse.from(transactions));
    }


}
