package com.example.IPS.IPS.repository;

import com.example.IPS.IPS.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionsRepo extends JpaRepository<Transactions, Long> {
    List<Transactions> findAllByTypeAndTimestampBetween(String type, LocalDateTime start, LocalDateTime end);

    List<Transactions> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<Transactions> findByStatus(String status);
<<<<<<< HEAD

    Transactions findByTransactionId(String id);


=======
    Transactions findByTransactionId(String id);

>>>>>>> 9eb53bfe95b70f01f7a15309dfbe2cd42346c61b
}
