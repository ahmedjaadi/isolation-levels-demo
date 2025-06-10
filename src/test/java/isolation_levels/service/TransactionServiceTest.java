package isolation_levels.service;

import isolation_levels.model.Account;
import isolation_levels.model.Transaction;
import isolation_levels.repository.AccountRepository;
import isolation_levels.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link TransactionService}.
 * Tests transaction creation, retrieval, and money transfers.
 *
 * @author JetBrains Junie
 */
@SpringBootTest
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private static final String FROM_ACCOUNT_NUMBER = "TEST001";
    private static final String TO_ACCOUNT_NUMBER = "TEST002";
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");

    @BeforeEach
    @Transactional
    public void setUp() {
        // Delete test accounts if they exist
        accountRepository.findByAccountNumber(FROM_ACCOUNT_NUMBER)
                .ifPresent(account -> accountRepository.delete(account));
        accountRepository.findByAccountNumber(TO_ACCOUNT_NUMBER)
                .ifPresent(account -> accountRepository.delete(account));

        // Create fresh test accounts
        accountService.createAccount(FROM_ACCOUNT_NUMBER, "From User", INITIAL_BALANCE);
        accountService.createAccount(TO_ACCOUNT_NUMBER, "To User", INITIAL_BALANCE);
    }

    @Test
    @Transactional
    public void testCreateTransaction() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        String description = "Test transaction";
        Transaction.TransactionType type = Transaction.TransactionType.CREDIT;

        // When
        Optional<Transaction> transactionOpt = transactionService.createTransaction(
                FROM_ACCOUNT_NUMBER, amount, description, type);

        // Then
        assertTrue(transactionOpt.isPresent());
        Transaction transaction = transactionOpt.get();
        assertNotNull(transaction.getId());
        assertEquals(amount, transaction.getAmount());
        assertEquals(description, transaction.getDescription());
        assertEquals(type, transaction.getType());
        assertNotNull(transaction.getTimestamp());
        assertNotNull(transaction.getAccount());
        assertEquals(FROM_ACCOUNT_NUMBER, transaction.getAccount().getAccountNumber());

        // Verify the account balance was updated
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(FROM_ACCOUNT_NUMBER);
        assertTrue(accountOpt.isPresent());
        assertEquals(INITIAL_BALANCE.add(amount), accountOpt.get().getBalance());
    }

    @Test
    @Transactional
    public void testGetTransactionsReadCommitted() {
        // Given
        BigDecimal amount1 = new BigDecimal("100.00");
        BigDecimal amount2 = new BigDecimal("200.00");
        transactionService.createTransaction(
                FROM_ACCOUNT_NUMBER, amount1, "Transaction 1", Transaction.TransactionType.CREDIT);
        transactionService.createTransaction(
                FROM_ACCOUNT_NUMBER, amount2, "Transaction 2", Transaction.TransactionType.CREDIT);

        // When
        List<Transaction> transactions = transactionService.getTransactionsReadCommitted(FROM_ACCOUNT_NUMBER);

        // Then
        assertEquals(2, transactions.size());
        assertTrue(transactions.stream().anyMatch(t -> t.getAmount().equals(amount1)));
        assertTrue(transactions.stream().anyMatch(t -> t.getAmount().equals(amount2)));
    }

    @Test
    @Transactional
    public void testTransferMoney() {
        // Given
        BigDecimal transferAmount = new BigDecimal("500.00");

        // When
        boolean success = transactionService.transferMoney(FROM_ACCOUNT_NUMBER, TO_ACCOUNT_NUMBER, transferAmount);

        // Then
        assertTrue(success);

        // Verify the account balances were updated
        Optional<Account> fromAccountOpt = accountRepository.findByAccountNumber(FROM_ACCOUNT_NUMBER);
        Optional<Account> toAccountOpt = accountRepository.findByAccountNumber(TO_ACCOUNT_NUMBER);
        
        assertTrue(fromAccountOpt.isPresent());
        assertTrue(toAccountOpt.isPresent());
        
        assertEquals(INITIAL_BALANCE.subtract(transferAmount), fromAccountOpt.get().getBalance());
        assertEquals(INITIAL_BALANCE.add(transferAmount), toAccountOpt.get().getBalance());

        // Verify transactions were created
        List<Transaction> fromAccountTransactions = transactionRepository.findByAccount(fromAccountOpt.get());
        List<Transaction> toAccountTransactions = transactionRepository.findByAccount(toAccountOpt.get());
        
        assertEquals(1, fromAccountTransactions.size());
        assertEquals(1, toAccountTransactions.size());
        
        assertEquals(transferAmount, fromAccountTransactions.get(0).getAmount());
        assertEquals(Transaction.TransactionType.DEBIT, fromAccountTransactions.get(0).getType());
        
        assertEquals(transferAmount, toAccountTransactions.get(0).getAmount());
        assertEquals(Transaction.TransactionType.CREDIT, toAccountTransactions.get(0).getType());
    }

    @Test
    @Transactional
    public void testTransferMoneyInsufficientFunds() {
        // Given
        BigDecimal transferAmount = new BigDecimal("1500.00"); // More than initial balance

        // When
        boolean success = transactionService.transferMoney(FROM_ACCOUNT_NUMBER, TO_ACCOUNT_NUMBER, transferAmount);

        // Then
        assertFalse(success);

        // Verify the account balances were not changed
        Optional<Account> fromAccountOpt = accountRepository.findByAccountNumber(FROM_ACCOUNT_NUMBER);
        Optional<Account> toAccountOpt = accountRepository.findByAccountNumber(TO_ACCOUNT_NUMBER);
        
        assertTrue(fromAccountOpt.isPresent());
        assertTrue(toAccountOpt.isPresent());
        
        assertEquals(INITIAL_BALANCE, fromAccountOpt.get().getBalance());
        assertEquals(INITIAL_BALANCE, toAccountOpt.get().getBalance());

        // Verify no transactions were created
        List<Transaction> fromAccountTransactions = transactionRepository.findByAccount(fromAccountOpt.get());
        List<Transaction> toAccountTransactions = transactionRepository.findByAccount(toAccountOpt.get());
        
        assertEquals(0, fromAccountTransactions.size());
        assertEquals(0, toAccountTransactions.size());
    }

    @Test
    @Transactional
    public void testGetRecentTransactions() {
        // Given
        transactionService.createTransaction(
                FROM_ACCOUNT_NUMBER, new BigDecimal("100.00"), "Transaction 1", Transaction.TransactionType.CREDIT);
        transactionService.createTransaction(
                FROM_ACCOUNT_NUMBER, new BigDecimal("200.00"), "Transaction 2", Transaction.TransactionType.CREDIT);

        // When
        List<Transaction> transactions = transactionService.getTransactionsByAccountNumberOrderByTimestampDesc(FROM_ACCOUNT_NUMBER);

        // Then
        assertEquals(2, transactions.size());
        // Verify transactions are ordered by timestamp (newest first)
        assertTrue(transactions.get(0).getTimestamp().isAfter(transactions.get(1).getTimestamp()) ||
                   transactions.get(0).getTimestamp().equals(transactions.get(1).getTimestamp()));
    }
}