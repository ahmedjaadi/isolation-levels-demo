package isolation_levels.service;

import isolation_levels.model.Account;
import isolation_levels.model.Transaction;
import isolation_levels.repository.AccountRepository;
import isolation_levels.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing {@link Account} entities.
 * This class demonstrates different transaction isolation levels.
 *
 * @author JetBrains Junie
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Creates a new account with the specified details.
     *
     * @param accountNumber the unique account number
     * @param ownerName the name of the account owner
     * @param initialBalance the initial balance of the account
     * @return the created account
     */
    @Transactional
    public Account createAccount(String accountNumber, String ownerName, BigDecimal initialBalance) {
        Account account = new Account(accountNumber, ownerName, initialBalance);
        return accountRepository.save(account);
    }

    /**
     * Retrieves an account by its account number using READ_UNCOMMITTED isolation level.
     * This can lead to dirty reads.
     *
     * @param accountNumber the account number to search for
     * @return an Optional containing the account if found, or empty if not found
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Optional<Account> getAccountReadUncommitted(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    /**
     * Retrieves an account by its account number using READ_COMMITTED isolation level.
     * This prevents dirty reads but allows non-repeatable reads and phantom reads.
     *
     * @param accountNumber the account number to search for
     * @return an Optional containing the account if found, or empty if not found
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<Account> getAccountReadCommitted(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    /**
     * Retrieves an account by its account number using REPEATABLE_READ isolation level.
     * This prevents dirty reads and non-repeatable reads but allows phantom reads.
     *
     * @param accountNumber the account number to search for
     * @return an Optional containing the account if found, or empty if not found
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Optional<Account> getAccountRepeatableRead(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    /**
     * Retrieves an account by its account number using SERIALIZABLE isolation level.
     * This prevents dirty reads, non-repeatable reads, and phantom reads.
     *
     * @param accountNumber the account number to search for
     * @return an Optional containing the account if found, or empty if not found
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Optional<Account> getAccountSerializable(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    /**
     * Updates an account's balance with READ_UNCOMMITTED isolation level.
     * This can lead to dirty reads, non-repeatable reads, and phantom reads.
     *
     * @param accountNumber the account number to update
     * @param newBalance the new balance to set
     * @return the updated account, or empty if the account was not found
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Optional<Account> updateBalanceReadUncommitted(String accountNumber, BigDecimal newBalance) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            account.setBalance(newBalance);
            return Optional.of(accountRepository.save(account));
        }
        return Optional.empty();
    }

    /**
     * Updates an account's balance with READ_COMMITTED isolation level.
     * This prevents dirty reads but allows non-repeatable reads and phantom reads.
     *
     * @param accountNumber the account number to update
     * @param newBalance the new balance to set
     * @return the updated account, or empty if the account was not found
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<Account> updateBalanceReadCommitted(String accountNumber, BigDecimal newBalance) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            account.setBalance(newBalance);
            return Optional.of(accountRepository.save(account));
        }
        return Optional.empty();
    }

    /**
     * Updates an account's balance with REPEATABLE_READ isolation level.
     * This prevents dirty reads and non-repeatable reads but allows phantom reads.
     *
     * @param accountNumber the account number to update
     * @param newBalance the new balance to set
     * @return the updated account, or empty if the account was not found
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Optional<Account> updateBalanceRepeatableRead(String accountNumber, BigDecimal newBalance) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            account.setBalance(newBalance);
            return Optional.of(accountRepository.save(account));
        }
        return Optional.empty();
    }

    /**
     * Updates an account's balance with SERIALIZABLE isolation level.
     * This prevents dirty reads, non-repeatable reads, and phantom reads.
     *
     * @param accountNumber the account number to update
     * @param newBalance the new balance to set
     * @return the updated account, or empty if the account was not found
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Optional<Account> updateBalanceSerializable(String accountNumber, BigDecimal newBalance) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            account.setBalance(newBalance);
            return Optional.of(accountRepository.save(account));
        }
        return Optional.empty();
    }

    /**
     * Retrieves all accounts.
     *
     * @return a list of all accounts
     */
    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Demonstrates optimistic locking by updating an account's balance.
     * This method relies on the @Version annotation in the Account entity.
     *
     * @param accountNumber the account number to update
     * @param amount the amount to add to the balance (can be negative)
     * @return the updated account, or empty if the account was not found
     */
    @Transactional
    public Optional<Account> updateBalanceWithOptimisticLock(String accountNumber, BigDecimal amount) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            BigDecimal newBalance = account.getBalance().add(amount);
            account.setBalance(newBalance);
            
            // Create a transaction record
            Transaction transaction = new Transaction(
                amount,
                amount.compareTo(BigDecimal.ZERO) >= 0 ? "Deposit" : "Withdrawal",
                amount.compareTo(BigDecimal.ZERO) >= 0 ? Transaction.TransactionType.CREDIT : Transaction.TransactionType.DEBIT
            );
            account.addTransaction(transaction);
            
            return Optional.of(accountRepository.save(account));
        }
        return Optional.empty();
    }

    /**
     * Demonstrates pessimistic locking by updating an account's balance.
     * This method uses a pessimistic write lock to prevent concurrent updates.
     *
     * @param accountNumber the account number to update
     * @param amount the amount to add to the balance (can be negative)
     * @return the updated account, or empty if the account was not found
     */
    @Transactional
    public Optional<Account> updateBalanceWithPessimisticLock(String accountNumber, BigDecimal amount) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumberWithPessimisticWriteLock(accountNumber);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            BigDecimal newBalance = account.getBalance().add(amount);
            account.setBalance(newBalance);
            
            // Create a transaction record
            Transaction transaction = new Transaction(
                amount,
                amount.compareTo(BigDecimal.ZERO) >= 0 ? "Deposit" : "Withdrawal",
                amount.compareTo(BigDecimal.ZERO) >= 0 ? Transaction.TransactionType.CREDIT : Transaction.TransactionType.DEBIT
            );
            account.addTransaction(transaction);
            
            return Optional.of(accountRepository.save(account));
        }
        return Optional.empty();
    }
}