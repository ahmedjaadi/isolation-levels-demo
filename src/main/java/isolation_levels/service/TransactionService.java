package isolation_levels.service;

import isolation_levels.model.Account;
import isolation_levels.model.Transaction;
import isolation_levels.repository.AccountRepository;
import isolation_levels.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing {@link Transaction} entities.
 * This class demonstrates different transaction isolation levels.
 *
 * @author JetBrains Junie
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Creates a new transaction for an account.
     *
     * @param accountNumber the account number
     * @param amount the transaction amount
     * @param description the transaction description
     * @param type the transaction type (DEBIT or CREDIT)
     * @return the created transaction, or empty if the account was not found
     */
    @Transactional
    public Optional<Transaction> createTransaction(String accountNumber, BigDecimal amount, 
                                                  String description, Transaction.TransactionType type) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            
            // Update account balance
            if (type == Transaction.TransactionType.CREDIT) {
                account.setBalance(account.getBalance().add(amount));
            } else {
                account.setBalance(account.getBalance().subtract(amount));
            }
            
            // Create and save transaction
            Transaction transaction = new Transaction(amount, description, type);
            account.addTransaction(transaction);
            
            accountRepository.save(account);
            return Optional.of(transaction);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all transactions for an account using READ_UNCOMMITTED isolation level.
     * This can lead to dirty reads.
     *
     * @param accountNumber the account number
     * @return a list of transactions for the account, or empty list if the account was not found
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public List<Transaction> getTransactionsReadUncommitted(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        return accountOpt.map(transactionRepository::findByAccount).orElse(List.of());
    }

    /**
     * Retrieves all transactions for an account using READ_COMMITTED isolation level.
     * This prevents dirty reads but allows non-repeatable reads and phantom reads.
     *
     * @param accountNumber the account number
     * @return a list of transactions for the account, or empty list if the account was not found
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<Transaction> getTransactionsReadCommitted(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        return accountOpt.map(transactionRepository::findByAccount).orElse(List.of());
    }

    /**
     * Retrieves all transactions for an account using REPEATABLE_READ isolation level.
     * This prevents dirty reads and non-repeatable reads but allows phantom reads.
     *
     * @param accountNumber the account number
     * @return a list of transactions for the account, or empty list if the account was not found
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<Transaction> getTransactionsRepeatableRead(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        return accountOpt.map(transactionRepository::findByAccount).orElse(List.of());
    }

    /**
     * Retrieves all transactions for an account using SERIALIZABLE isolation level.
     * This prevents dirty reads, non-repeatable reads, and phantom reads.
     *
     * @param accountNumber the account number
     * @return a list of transactions for the account, or empty list if the account was not found
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<Transaction> getTransactionsSerializable(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        return accountOpt.map(transactionRepository::findByAccount).orElse(List.of());
    }

    /**
     * Retrieves all transactions for an account, ordered by timestamp (newest first).
     *
     * @param accountNumber the account number
     * @return a list of transactions for the account, ordered by timestamp, or empty list if the account was not found
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccountNumberOrderByTimestampDesc(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        return accountOpt.map(transactionRepository::findByAccountOrderByTimestampDesc).orElse(List.of());
    }

    /**
     * Transfers money between two accounts using SERIALIZABLE isolation level to ensure consistency.
     *
     * @param fromAccountNumber the account number to transfer from
     * @param toAccountNumber the account number to transfer to
     * @param amount the amount to transfer
     * @return true if the transfer was successful, false otherwise
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean transferMoney(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false; // Amount must be positive
        }
        
        Optional<Account> fromAccountOpt = accountRepository.findByAccountNumber(fromAccountNumber);
        Optional<Account> toAccountOpt = accountRepository.findByAccountNumber(toAccountNumber);
        
        if (fromAccountOpt.isEmpty() || toAccountOpt.isEmpty()) {
            return false; // One or both accounts not found
        }
        
        Account fromAccount = fromAccountOpt.get();
        Account toAccount = toAccountOpt.get();
        
        // Check if the from account has sufficient funds
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            return false; // Insufficient funds
        }
        
        // Update balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        
        // Create transactions
        Transaction debitTransaction = new Transaction(amount, "Transfer to " + toAccountNumber, Transaction.TransactionType.DEBIT);
        Transaction creditTransaction = new Transaction(amount, "Transfer from " + fromAccountNumber, Transaction.TransactionType.CREDIT);
        
        fromAccount.addTransaction(debitTransaction);
        toAccount.addTransaction(creditTransaction);
        
        // Save accounts
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        return true;
    }
}