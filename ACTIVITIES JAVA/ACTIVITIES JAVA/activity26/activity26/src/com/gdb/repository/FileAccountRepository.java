package com.gdb.repository;

import com.gdb.domain.*;
import com.gdb.exceptions.DataAccessException;
import com.gdb.file.AtomicFileWriter;
import com.gdb.file.BackupManager;
import com.gdb.file.CsvMapper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * File-backed implementation of AccountRepository.
 * Stores account records in a CSV/text file with atomic writes.
 */
public class FileAccountRepository implements AccountRepository {

    private final String filePath;
    private final boolean autosave;
    private final boolean backupEnabled;
    private final int maxBackups;
    private int nextAccountNumber;
    private final Map<Integer, IAccount> cache = new HashMap<>();

    public FileAccountRepository(String filePath, boolean autosave, boolean backupEnabled, int maxBackups, int startAccountNumber) {
        this.filePath = filePath;
        this.autosave = autosave;
        this.backupEnabled = backupEnabled;
        this.maxBackups = maxBackups;
        this.nextAccountNumber = startAccountNumber;
        load();
    }

    public FileAccountRepository(String filePath, boolean param2, int param3) {
        this.filePath = filePath;
        if (param3 >= 1000) {
            this.autosave = param2;
            this.backupEnabled = true;
            this.maxBackups = 5;
            this.nextAccountNumber = param3;
        } else {
            this.autosave = true;
            this.backupEnabled = param2;
            this.maxBackups = param3;
            this.nextAccountNumber = 1001;
        }
        load();
    }

    public FileAccountRepository(String filePath, int startAccountNumber) {
        this(filePath, true, true, 5, startAccountNumber);
    }

    public FileAccountRepository(String filePath) {
        this(filePath, true, true, 5, 1001);
    }

    // ============================================================
    // 📝 STEP 1: Implement load()
    //
    // INSTRUCTIONS:
    //   Read accounts from filePath and populate the memory cache.
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
                if (line == null || line.trim().isEmpty() || line.trim().startsWith("account_number")) {
                    continue;
                }
                IAccount acc = CsvMapper.fromCsv(line);
                if (acc != null) {
                    cache.put(acc.getAccountNumber(), acc);
                    if (acc.getAccountNumber() >= nextAccountNumber) {
                        nextAccountNumber = acc.getAccountNumber() + 1;
                    }
                }
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to load accounts from file: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 📝 STEP 2: Implement flush()
    //
    // INSTRUCTIONS:
    //   Serialize cache to CSV lines and write using AtomicFileWriter.
    // ============================================================
    public synchronized void flush() {
        try {
            if (backupEnabled && Files.exists(Paths.get(filePath))) {
                BackupManager.backup(filePath, maxBackups);
            }
            List<String> lines = new ArrayList<>();
            lines.add(CsvMapper.ACCOUNT_HEADER);
            List<IAccount> accounts = new ArrayList<>(cache.values());
            accounts.sort(Comparator.comparingInt(IAccount::getAccountNumber));
            for (IAccount acc : accounts) {
                lines.add(CsvMapper.toCsv(acc));
            }
            AtomicFileWriter.write(filePath, lines);
        } catch (IOException e) {
            throw new DataAccessException("Failed to flush accounts to file: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // 📝 STEP 3: Implement save(IAccount account)
    // ============================================================
    @Override
    public synchronized void save(IAccount account) {
        if (account == null) return;
        cache.put(account.getAccountNumber(), account);
        if (account.getAccountNumber() >= nextAccountNumber) {
            nextAccountNumber = account.getAccountNumber() + 1;
        }
        if (autosave) {
            flush();
        }
    }

    // ============================================================
    // 📝 STEP 4: Implement findById(int accountNumber)
    // ============================================================
    @Override
    public synchronized IAccount findById(int accountNumber) {
        return cache.get(accountNumber);
    }

    // ============================================================
    // 📝 STEP 5: Implement findAll()
    // ============================================================
    @Override
    public synchronized List<IAccount> findAll() {
        List<IAccount> list = new ArrayList<>(cache.values());
        list.sort(Comparator.comparingInt(IAccount::getAccountNumber));
        return list;
    }

    // ============================================================
    // 📝 STEP 6: Implement update(IAccount account)
    // ============================================================
    @Override
    public synchronized void update(IAccount account) {
        if (account != null && cache.containsKey(account.getAccountNumber())) {
            cache.put(account.getAccountNumber(), account);
            if (autosave) {
                flush();
            }
        }
    }

    // ============================================================
    // 📝 STEP 7: Implement delete(int accountNumber)
    // ============================================================
    @Override
    public synchronized void delete(int accountNumber) {
        if (cache.remove(accountNumber) != null) {
            if (autosave) {
                flush();
            }
        }
    }

    // ============================================================
    // 📝 STEP 8: Implement exists(int accountNumber)
    // ============================================================
    @Override
    public synchronized boolean exists(int accountNumber) {
        return cache.containsKey(accountNumber);
    }

    // ============================================================
    // 📝 STEP 9: Implement nextAccountNumber()
    // ============================================================
    @Override
    public synchronized int nextAccountNumber() {
        return nextAccountNumber++;
    }
}
