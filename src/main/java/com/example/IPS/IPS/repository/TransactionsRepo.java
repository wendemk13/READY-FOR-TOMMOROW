package com.example.IPS.IPS.repository;

import com.example.IPS.IPS.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionsRepo extends JpaRepository<Transactions, Long> {
    List<Transactions> findAllByTypeAndTimestampBetween(String type, LocalDateTime start, LocalDateTime end);

    List<Transactions> findAllByStatusAndTypeAndTimestampBetween(String status, String type, LocalDateTime start, LocalDateTime end);

    List<Transactions> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<Transactions> findByStatusAndTimestampBetween(String status, LocalDateTime start, LocalDateTime end);

    List<Transactions> findByTypeAndStatusAndTimestampBetween(String type, String status, LocalDateTime start, LocalDateTime end);

    Transactions findByTransactionId(String id);

    // Count all failed transactions today
    @Query("SELECT COUNT(t) FROM Transactions t WHERE t.status = 'FAILED' AND t.timestamp BETWEEN :start AND :end")
    long countFailedTransactions(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Count total transactions today
    @Query("SELECT COUNT(t) FROM Transactions t WHERE t.timestamp BETWEEN :start AND :end")
    long countTotalTransactions(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


}
