package com.gdb.repository;

import com.gdb.domain.Transaction;
import com.gdb.domain.TransactionType;
import com.gdb.exceptions.DataAccessException;
import com.gdb.file.AtomicFileWriter;
import com.gdb.file.BackupManager;
import com.gdb.file.CsvMapper;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * File-backed implementation of TransactionRepository.
 * Stores transaction records in a text file and supports atomic balance transfers.
 */
public class FileTransactionRepository implements TransactionRepository {

    private final String filePath;
    private final boolean autosave;
    private final boolean backupEnabled;
    private final int maxBackups;
    private String accountFilePath;
    private AccountRepository accountRepository;
    private final List<Transaction> cache = new ArrayList<>();

    public FileTransactionRepository(String filePath, boolean autosave, boolean backupEnabled, int maxBackups, int startId) {
        this.filePath = filePath;
        this.autosave = autosave;
        this.backupEnabled = backupEnabled;
        this.maxBackups = maxBackups;
        this.accountFilePath = Paths.get(filePath).resolveSibling("accounts.csv").toString();
        load();
    }

    public FileTransactionRepository(String filePath, boolean param2, int param3) {
        this.filePath = filePath;
        this.accountFilePath = Paths.get(filePath).resolveSibling("accounts.csv").toString();
        if (param3 > 100) {
            this.autosave = param2;
            this.backupEnabled = true;
            this.maxBackups = 5;
        } else {
            this.autosave = true;
            this.backupEnabled = param2;
            this.maxBackups = param3;
        }
        load();
    }

    public FileTransactionRepository(String filePath, int startId) {
        this(filePath, true, true, 5, startId);
    }

    public FileTransactionRepository(String filePath) {
        this(filePath, true, true, 5, 1);
    }

    public FileTransactionRepository(String filePath, AccountRepository accountRepository) {
        this(filePath);
        this.accountRepository = accountRepository;
    }

    public void setAccountRepository(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void setAccountFilePath(String accountFilePath) {
        this.accountFilePath = accountFilePath;
    }

    // ============================================================
    // 📝 STEP 1: Implement load() and flush()
    // ============================================================
    public synchronized void load() {
        cache.clear();
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || line.trim().isEmpty() || line.trim().startsWith("transaction_id")) {
                    continue;
                }
                Transaction txn = CsvMapper.fromCsvTransaction(line);
                if (txn != null) {
                    cache.add(txn);
                }
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to load transactions: " + e.getMessage(), e);
        }
    }

    public synchronized void flush() {
        try {
            if (backupEnabled && Files.exists(Paths.get(filePath))) {
                BackupManager.backup(filePath, maxBackups);
            }
            List<String> lines = new ArrayList<>();
            lines.add(CsvMapper.TRANSACTION_HEADER);
            for (Transaction txn : cache) {
                lines.add(CsvMapper.toCsv(txn));
            }
            AtomicFileWriter.write(filePath, lines);
        } catch (IOException e) {
            throw new DataAccessException("Failed to flush transactions: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 📝 STEP 2: Implement save(Transaction transaction)
    // ============================================================
    @Override
    public synchronized void save(Transaction transaction) {
        if (transaction != null) {
            cache.add(transaction);
            if (autosave) {
                flush();
            }
        }
    }

    // ============================================================
    // 📝 STEP 3: Implement findByAccount(int accountNumber)
    // ============================================================
    @Override
    public synchronized List<Transaction> findByAccount(int accountNumber) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction txn : cache) {
            if (txn.getAccountNumber() == accountNumber ||
                txn.getFromAccount() == accountNumber ||
                txn.getToAccount() == accountNumber) {
                result.add(txn);
            }
        }
        return result;
    }

    // ============================================================
    // 📝 STEP 4: Implement findAll()
    // ============================================================
    @Override
    public synchronized List<Transaction> findAll() {
        return new ArrayList<>(cache);
    }

    // ============================================================
    // 📝 STEP 5: Implement clear()
    // ============================================================
    @Override
    public synchronized void clear() {
        cache.clear();
        if (autosave) {
            flush();
        }
    }

    // ============================================================
    // 📝 STEP 6: Implement transfer(int fromAccountNumber, int toAccountNumber, double amount)
    // ============================================================
    @Override
    public synchronized void transfer(int fromAccountNumber, int toAccountNumber, double amount) {
        String accPath = accountFilePath;
        if (accPath == null) {
            Path p = Paths.get(filePath).resolveSibling("accounts.csv");
            accPath = p.toString();
        }

        Path accFile = Paths.get(accPath);
        if (!Files.exists(accFile)) {
            throw new DataAccessException("Account file does not exist: " + accPath);
        }

        try {
            List<String> lines = Files.readAllLines(accFile, java.nio.charset.StandardCharsets.UTF_8);
            List<String> updatedLines = new ArrayList<>();
            double senderNewBal = 0.0;
            double receiverNewBal = 0.0;
            boolean senderFound = false;
            boolean receiverFound = false;

            for (String line : lines) {
                if (line == null || line.trim().isEmpty() || line.trim().startsWith("account_number")) {
                    updatedLines.add(line);
                    continue;
                }
                String[] parts = line.split(",", -1);
                int accNo = Integer.parseInt(parts[0].trim());
                if (accNo == fromAccountNumber) {
                    senderFound = true;
                    double currentBal = Double.parseDouble(parts[3].trim());
                    if (currentBal < amount) {
                        throw new DataAccessException("Insufficient balance in account #" + fromAccountNumber);
                    }
                    senderNewBal = currentBal - amount;
                    parts[3] = String.valueOf(senderNewBal);
                    updatedLines.add(String.join(",", parts));
                } else if (accNo == toAccountNumber) {
                    receiverFound = true;
                    double currentBal = Double.parseDouble(parts[3].trim());
                    receiverNewBal = currentBal + amount;
                    parts[3] = String.valueOf(receiverNewBal);
                    updatedLines.add(String.join(",", parts));
                } else {
                    updatedLines.add(line);
                }
            }

            if (!senderFound || !receiverFound) {
                throw new DataAccessException("Sender or receiver account not found");
            }

            AtomicFileWriter.write(accPath, updatedLines);

            if (accountRepository != null) {
                com.gdb.domain.IAccount s = accountRepository.findById(fromAccountNumber);
                com.gdb.domain.IAccount r = accountRepository.findById(toAccountNumber);
                if (s != null && r != null) {
                    try {
                        java.lang.reflect.Field balField = com.gdb.domain.Account.class.getDeclaredField("balance");
                        balField.setAccessible(true);
                        balField.setDouble(s, senderNewBal);
                        balField.setDouble(r, receiverNewBal);
                    } catch (Exception ignored) {}
                }
            }

            Transaction txn = new Transaction(
                Transaction.generateId(),
                LocalDateTime.now(),
                fromAccountNumber,
                TransactionType.TRANSFER,
                amount,
                senderNewBal,
                "SUCCESS",
                "Transfer of Rs. " + amount + " to Account #" + toAccountNumber,
                fromAccountNumber,
                toAccountNumber
            );
            save(txn);
        } catch (IOException e) {
            throw new DataAccessException("Transfer failed: " + e.getMessage(), e);
        }
    }
}
