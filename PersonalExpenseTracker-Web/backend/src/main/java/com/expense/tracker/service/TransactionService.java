package com.expense.tracker.service;

import com.expense.tracker.dto.TransactionDTO;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.exception.ValidationException;
import com.expense.tracker.model.Transaction;
import com.expense.tracker.model.TransactionType;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<TransactionDTO> getAllTransactions(User user) {
        return transactionRepository.findByUserIdOrderByDateDescIdDesc(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO getTransactionById(User user, Long id) {
        Transaction t = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID " + id));
        return mapToDTO(t);
    }

    @Transactional
    public TransactionDTO createTransaction(User user, TransactionDTO dto) {
        validateTransactionDTO(dto);

        boolean isDemo = dto.isDemo();
        if (dto.getDescription() != null && dto.getDescription().contains(Transaction.DEMO_TAG)) {
            isDemo = true;
        }

        Transaction transaction = new Transaction(
                dto.getType(),
                dto.getAmount().setScale(2, RoundingMode.HALF_UP),
                dto.getCategory().trim(),
                dto.getDescription() != null ? dto.getDescription().trim() : "",
                dto.getDate() != null ? dto.getDate() : LocalDate.now(),
                isDemo,
                user
        );

        Transaction saved = transactionRepository.save(transaction);
        return mapToDTO(saved);
    }

    @Transactional
    public TransactionDTO updateTransaction(User user, Long id, TransactionDTO dto) {
        validateTransactionDTO(dto);

        Transaction t = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID " + id));

        t.setType(dto.getType());
        t.setAmount(dto.getAmount().setScale(2, RoundingMode.HALF_UP));
        t.setCategory(dto.getCategory().trim());
        t.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        t.setDate(dto.getDate());

        Transaction updated = transactionRepository.save(t);
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteTransaction(User user, Long id) {
        Transaction t = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID " + id));
        transactionRepository.delete(t);
    }

    public List<TransactionDTO> searchTransactions(User user, String query, TransactionType type, String category,
                                                  LocalDate startDate, LocalDate endDate,
                                                  BigDecimal minAmount, BigDecimal maxAmount) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String cleanCat = (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category)) ? category.trim() : null;

        return transactionRepository.searchTransactions(user.getId(), cleanQuery, type, cleanCat, startDate, endDate, minAmount, maxAmount)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public int deleteDemoTransactions(User user) {
        return transactionRepository.deleteByUserIdAndIsDemoTrue(user.getId());
    }

    private void validateTransactionDTO(TransactionDTO dto) {
        if (dto.getType() == null) {
            throw new ValidationException("Transaction type (INCOME or EXPENSE) is required");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
        if (dto.getCategory() == null || dto.getCategory().trim().isEmpty()) {
            throw new ValidationException("Category is required");
        }
        if (dto.getDate() == null) {
            throw new ValidationException("Date is required");
        }
    }

    public TransactionDTO mapToDTO(Transaction t) {
        return new TransactionDTO(
                t.getId(),
                t.getType(),
                t.getAmount(),
                t.getCategory(),
                t.getDescription(),
                t.getDate(),
                t.isDemo()
        );
    }
}
