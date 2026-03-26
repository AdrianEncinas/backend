package com.assetstrack.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.assetstrack.backend.model.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByHoldingUserId(Long userId);
    
    @Query("SELECT t FROM Transaction t WHERE t.holding.user.id = :userId ORDER BY t.date ASC")
    List<Transaction> findAllByUserIdOrderByDateAsc(Long userId);
}